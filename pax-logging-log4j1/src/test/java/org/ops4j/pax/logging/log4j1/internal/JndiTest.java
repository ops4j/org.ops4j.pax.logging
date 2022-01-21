/*
 * Copyright 2022 OPS4J.
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
package org.ops4j.pax.logging.log4j1.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.directory.InitialDirContext;
import javax.naming.spi.DirectoryManager;
import javax.naming.spi.NamingManager;

import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.asn1.ber.Asn1Decoder;
import org.apache.directory.api.asn1.util.Asn1Buffer;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.api.LdapEncoder;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.osgi.DefaultLdapCodecService;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.BindResponseImpl;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchResultDoneImpl;
import org.apache.directory.api.ldap.model.message.SearchResultEntryImpl;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class JndiTest {

    private static final LdapApiService LDAP = new DefaultLdapCodecService();

    @Test(expected = NoInitialContextException.class)
    public void plainInitialContext() throws NamingException {
        InitialContext ctx = new InitialContext();
        Object hello = ctx.lookup("Hello");
    }

    @Test(expected = NoInitialContextException.class)
    public void plainInitialDirContext() throws NamingException {
        InitialContext ctx = new InitialDirContext();
        Object hello = ctx.lookup("Hello");
    }

    @Test
    @Ignore
    public void plainDiscoveredContext() throws NamingException {
        {
            // com.sun.jndi.url.ldap.ldapURLContext
            Context ctx = NamingManager.getURLContext("ldap", null);
            // -> javax.naming.spi.NamingManager.getURLObject("ldap", null, null, null, env)
            Object hello = ctx.lookup("ldap://localhost:389");
            System.out.println("hello: " + hello);
        }

        {
            // com.sun.jndi.url.ldap.ldapURLContext
            Context ctx = DirectoryManager.getURLContext("ldap", null);
            // -> javax.naming.spi.NamingManager.getURLObject("ldap", null, null, null, env)
            Object hello = ctx.lookup("ldap://localhost:389");
            System.out.println("hello: " + hello);
        }
    }

    @Test
    public void fakeLdapServer() throws IOException, NamingException {
        ServerSocketChannel ss = ServerSocketChannel.open();
        ss.bind(new InetSocketAddress("127.0.0.1", 0));
        int port = ((InetSocketAddress) ss.getLocalAddress()).getPort();
        System.out.println("Listening at " + port + " port");
        Thread t = new Thread(() -> {
            while (true) {
                SocketChannel s;
                try {
                    s = ss.accept();
                    ByteBuffer in = ByteBuffer.allocate(4096);
                    s.read(in);
                    in.mark();
                    in.position(0);
                    LdapMessageContainer<Message> container = new LdapMessageContainer<>(LDAP);
                    Asn1Decoder.decode(in, container);
                    System.out.println(container.getMessage());
                    if (container.getMessage().getType() == MessageTypeEnum.BIND_REQUEST) {
                        // bind
                        Asn1Buffer resp = new Asn1Buffer();
                        BindResponseImpl bindFine = new BindResponseImpl(container.getMessage().getMessageId());
                        LdapEncoder.encodeMessage(resp, LDAP, bindFine);
                        s.write(resp.getBytes());
                    }

                    in.rewind();
                    s.read(in);
                    in.mark();
                    in.position(0);
                    container = new LdapMessageContainer<>(LDAP);
                    Asn1Decoder.decode(in, container);
                    System.out.println(container.getMessage());
                    if (container.getMessage().getType() == MessageTypeEnum.SEARCH_REQUEST) {
                        // search
                        ((SearchRequest) container.getMessage()).getBase();
                        Asn1Buffer resp = new Asn1Buffer();
                        SearchResultEntryImpl searchFine = new SearchResultEntryImpl(container.getMessage().getMessageId());
                        DefaultEntry entry = new DefaultEntry();
                        entry.add("javaClassName", "java.lang.String");
                        ByteArrayOutputStream object = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(object);
                        oos.writeObject("Hello!");
                        oos.close();
                        entry.add("javaSerializedData", object.toByteArray());
                        searchFine.setEntry(entry);
                        LdapEncoder.encodeMessage(resp, LDAP, searchFine);
                        s.write(resp.getBytes());
                        // end of search
                        resp = new Asn1Buffer();
                        SearchResultDoneImpl searchEnd = new SearchResultDoneImpl(container.getMessage().getMessageId());
                        LdapEncoder.encodeMessage(resp, LDAP, searchEnd);
                        s.write(resp.getBytes());
                    }
                } catch (ClosedChannelException e) {
                    return;
                } catch (IOException | DecoderException | EncoderException | LdapException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, com.sun.jndi.ldap.LdapCtxFactory.class.getName());
        props.setProperty(Context.PROVIDER_URL, "ldap://localhost:" + port);

        // ctx.defaultInitCtx is com.sun.jndi.ldap.LdapCtx
        InitialContext ctx = new InitialDirContext(props);
        // thanks to "javaClassName" attribute, com.sun.jndi.ldap.Obj.decodeObject() is called on all the attributes
        // everything starts in com.sun.jndi.ldap.LdapCtx.c_lookup()
        String hello = (String) ctx.lookup("cn=test");
        assertThat(hello, equalTo("Hello!"));
        System.out.println("Response: " + hello);

        t.interrupt();
        ss.close();
    }

}
