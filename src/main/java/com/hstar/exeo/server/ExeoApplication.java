package com.hstar.exeo.server;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;

@SpringBootApplication
@EntityScan( basePackages = "com.hstar.exeo")
public class ExeoApplication implements EmbeddedServletContainerCustomizer {

	public static final String DEVELOPMENT_PROFILE = "development";

	private final int httpPort;
	private final int httpsPort;

	@Autowired
	public ExeoApplication(Environment env) {
		boolean isDevelopmentMode = Arrays.stream(env.getActiveProfiles()).anyMatch(s -> s.equals(DEVELOPMENT_PROFILE));
		this.httpPort = isDevelopmentMode ? 80 : 8080;
		this.httpsPort = isDevelopmentMode ? 443 : 8443;
	}


	public static void main(String[] args) {
		SpringApplication.run(ExeoApplication.class, args);
	}

	@Bean
	public JettyServerCustomizer jettyServerCustomizer() {
		return server -> {
			// gzip
			for(Handler h : server.getHandlers()) {
				if(h instanceof ServletContextHandler) {
					GzipHandler gh = new GzipHandler();
					gh.setMinGzipSize(0);
					gh.addIncludedMimeTypes("text/html", "text/plain", "text/css", "application/javascript", "application/json");
					gh.setMinGzipSize(512);
					gh.addIncludedPaths("/*");
					gh.setCheckGzExists(false);
					((ServletContextHandler)h).setGzipHandler(gh);
				}
			}

			// http/2 and ssl

			// HTTP Configuration
			HttpConfiguration http_config = new HttpConfiguration();
			http_config.setSecureScheme("https");
			http_config.setSecurePort(httpsPort);
			http_config.setSendXPoweredBy(true);
			http_config.setSendServerVersion(true);

			// HTTP 1.1/2 Connector (non ssl)
			ServerConnector http = new ServerConnector(server,new HttpConnectionFactory(http_config), new HTTP2CServerConnectionFactory(http_config));
			http.setPort(httpPort);
			server.setConnectors(new Connector[]{http});

			// ssl cert setup
			Security.addProvider(new BouncyCastleProvider());
			SslContextFactory sslContextFactory = new SslContextFactory();
			sslContextFactory.setKeyStorePath("keystore.jks");
			sslContextFactory.setKeyStorePassword("password");
			sslContextFactory.setKeyManagerPassword("password");
			sslContextFactory.setIncludeCipherSuites(
					"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
					"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
					"TLS_ECDHE_ECDSA_WITH_CHACHA_20_POLY1305",
					"TLS_ECDHE_RSA_WITH_CHACHA_20_POLY1305",
					"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
					"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
					"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
					"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
					"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
					"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256");
			sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);

			// HTTP/1.1 support.
			HttpConnectionFactory http1 = new HttpConnectionFactory(http_config);
			http_config.addCustomizer(new SecureRequestCustomizer());

			ArrayList<ConnectionFactory> connectionFactories = new ArrayList<>();

			if(http2_enabled != null && http2_enabled.equals("true")) {
				// HTTP/2
				HTTP2ServerConnectionFactory http2 = new HTTP2ServerConnectionFactory(http_config);

				NegotiatingServerConnectionFactory.checkProtocolNegotiationAvailable();
				ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
				alpn.setDefaultProtocol(http.getDefaultProtocol());

				SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory,alpn.getProtocol());

				connectionFactories.addAll(Arrays.asList(ssl, alpn, http2));
			} else {
				SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory,http1.getProtocol());

				connectionFactories.add(ssl);
			}

			connectionFactories.add(http1);
			ServerConnector connector = new ServerConnector(server,connectionFactories.toArray(new ConnectionFactory[connectionFactories.size()]));
			connector.setPort(httpsPort);
			server.addConnector(connector);
		};
	}

	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
		if (container instanceof JettyEmbeddedServletContainerFactory) {
			((JettyEmbeddedServletContainerFactory) container).addServerCustomizers(jettyServerCustomizer());
		}
	}

	@Bean
	public JedisConnectionFactory jedisConnectionFactory() {
		return new JedisConnectionFactory();
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer() {
		RedisMessageListenerContainer mlc = new RedisMessageListenerContainer();
		mlc.setConnectionFactory(jedisConnectionFactory());
		return mlc;
	}

	@Value("${exeo.http2.enabled}")
	private String http2_enabled;
    @Value("${exeo.keystore.location}")
    private String keystore_location;
    @Value("${exeo.keystore.password}")
    private String keystore_password;
    @Value("${exeo.keymanager.password}")
    private String keymanager_password;

}