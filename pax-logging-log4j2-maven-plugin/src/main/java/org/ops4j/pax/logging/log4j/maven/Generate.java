/*
 * Copyright 2020 OPS4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.logging.log4j.maven;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;

import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.util.PluginRegistry;
import org.apache.logging.log4j.core.config.plugins.util.ResolverUtil;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;

/**
 * Log4j2 has deprecated {@code org.apache.logging.log4j.core.config.plugins.util.PluginManager#addPackage()} method,
 * so we need to allow Log4j to discover another location of {@code Log4j2Plugins.dat} file.
 * This plugin generates such file into a Jar which will be added to {@code Bundle-ClassPath} OSGi manifest header
 * (because we can't override the file coming from log4j-core...).
 */
@Mojo(name = "generate-log4j-plugin-descriptor", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true, aggregator = true)
public class Generate extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Inject
    private RepositorySystem system;

    @Parameter
    private String[] plugins;

    @Override
    public void execute() {
        // prepare classpath
        Set<String> locations = new LinkedHashSet<>();
        locations.add(project.getBuild().getOutputDirectory());
        ArtifactHandler artifactHandler = project.getArtifact().getArtifactHandler();
        for (Dependency d : project.getDependencies()) {
            ArtifactResolutionRequest req = new ArtifactResolutionRequest();
            DefaultArtifact artifact = new DefaultArtifact(d.getGroupId(), d.getArtifactId(), d.getVersion(), d.getScope(), d.getType(), d.getClassifier(), artifactHandler);
            req.setArtifact(artifact);
            ArtifactResolutionResult result = system.resolve(req);
            if (!result.hasExceptions()) {
                for (Artifact a : result.getArtifacts()) {
                    locations.add(a.getFile().getAbsolutePath());
                }
            }
        }

        List<URL> cp = new ArrayList<>();
        for (String loc : locations) {
            try {
                cp.add(new File(loc).toURI().toURL());
            } catch (MalformedURLException ignored) {
            }
        }

        Set<Class<?>> classes = new LinkedHashSet<>();
        List<Plugin> foundAnnotations = new LinkedList<>();
        Map<Plugin, Class<?>> a2c = new IdentityHashMap<>();
        try (URLClassLoader cl = new URLClassLoader(cp.toArray(new URL[0]), Plugin.class.getClassLoader())) {
            for (String plugin : plugins) {
                try {
                    Class<?> pluginClass = cl.loadClass(plugin);
                    getLog().info("Analyzing package " + pluginClass.getPackage().getName());
                    collectPlugins(cl, pluginClass.getPackage(), foundAnnotations, a2c);
                } catch (ClassNotFoundException e) {
                    getLog().warn("Can't load " + plugin + ":" + e.getMessage());
                }
            }
        } catch (IOException ignored) {
        }

        // keyed by plugin category
        final Map<String, List<Plugin>> pluginMap = new LinkedHashMap<>();
        foundAnnotations.forEach(p -> pluginMap.computeIfAbsent(p.category(), k -> new ArrayList<>()).add(p));

        // see org.apache.logging.log4j.core.config.plugins.processor.PluginCache.loadCacheFiles()
        File log4jPluginDataJar = new File(project.getBuild().getOutputDirectory(), "META-INF/pax-logging-log4j-plugins/META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat");
        log4jPluginDataJar.getParentFile().mkdirs();
        try (final DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(log4jPluginDataJar.toPath())))) {
            out.writeInt(pluginMap.size());
            for (Map.Entry<String, List<Plugin>> entry : pluginMap.entrySet()) {
                String category = entry.getKey();
                List<Plugin> pluginsInCategory = entry.getValue();
                out.writeUTF(category.toLowerCase(Locale.ROOT));
                out.writeInt(pluginsInCategory.size());
                for (Plugin plugin : pluginsInCategory) {
                    // Must always read all parts of the entry, even if not adding, so that the stream progresses
                    // see org.apache.logging.log4j.core.config.plugins.processor.PluginProcessor.PluginElementVisitor.visitType()
                    out.writeUTF(plugin.name().toLowerCase(Locale.ROOT));
                    out.writeUTF(a2c.get(plugin).getName());
                    out.writeUTF(Plugin.EMPTY.equals(plugin.elementType()) ? plugin.name() : plugin.elementType());
                    out.writeBoolean(plugin.printObject());
                    out.writeBoolean(plugin.deferChildren());

                }
            }
        } catch (IOException e) {
            getLog().error("Can't write jar file with Pax Logging Lo4j2 annotation data", e);
        }
    }

    private void collectPlugins(ClassLoader loader, Package pkg, List<Plugin> foundAnnotations, Map<Plugin, Class<?>> a2c) {
        final ResolverUtil resolver = new ResolverUtil();
        resolver.setClassLoader(loader);
        resolver.findInPackage(new PluginRegistry.PluginTest(), pkg.getName());

        for (Class<?> cls : resolver.getClasses()) {
            final Plugin annotation = cls.getAnnotation(Plugin.class);
            if (annotation != null) {
                getLog().info("   Found plugin " + cls.getName());
                foundAnnotations.add(annotation);
                a2c.put(annotation, cls);
            }
        }
    }

}
