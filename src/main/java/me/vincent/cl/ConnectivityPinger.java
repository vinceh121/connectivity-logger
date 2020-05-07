package me.vincent.cl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class ConnectivityPinger {
	private final List<String> addresses;
	private final Collection<Consumer<PingResult>> listeners;
	private final CloseableHttpClient client;
	private Thread thread;
	private boolean started = false, useHttps = true;
	private long interval = 1000;

	public ConnectivityPinger() {
		this.addresses = new ArrayList<>();
		this.listeners = new ArrayList<>();
		this.client = HttpClients.custom()
				.disableCookieManagement()
				.disableAutomaticRetries()
				.disableRedirectHandling()
				.setMaxConnPerRoute(Integer.MAX_VALUE) // FIXME what
				.setMaxConnTotal(Integer.MAX_VALUE)
				.evictIdleConnections(3, TimeUnit.SECONDS)
				.build();
	}

	public Collection<PingResult> pingAll() {
		final Collection<PingResult> col = new ArrayList<>();
		for (final String a : this.addresses) {
			col.add(this.ping(a));
		}
		return col;
	}

	public PingResult ping(final String addr) {
		final PingResult res = new PingResult();
		res.setRawAddr(addr);
		try {
			res.setAddress(InetAddress.getByName(addr));
		} catch (final UnknownHostException e1) {
			e1.printStackTrace();
			res.setDnsError(e1.toString());
		}

		final HttpGet get = new HttpGet((this.useHttps ? "https" : "http") + "://" + addr + "/");
		final HttpResponse r;
		try {
			r = this.client.execute(get);
		} catch (final IOException e) {
			e.printStackTrace();
			res.setHTTPError(e.toString());
			return res;
		}

		res.setHttpStatus(r.getStatusLine().getStatusCode());

		return res;
	}

	public void start() {
		if (this.thread != null) {
			this.thread.interrupt();
		}
		this.thread = new Thread((Runnable) () -> {
			while (ConnectivityPinger.this.started) {
				System.out.println("Pinging");
				ConnectivityPinger.this.firePings(ConnectivityPinger.this.pingAll());
				System.out.println("Done");
				try {
					Thread.sleep(ConnectivityPinger.this.interval);
				} catch (final InterruptedException e) {
					System.out.println("Pinger thread interrupted");
				}
			}
		}, "pinging-thread");
		this.thread.start();
		this.started = true;
	}

	public void addListener(final Consumer<PingResult> cons) {
		this.listeners.add(cons);
	}

	public void removeListener(final Consumer<PingResult> cons) {
		this.listeners.remove(cons);
	}

	public void removeListenerType(final Class<?> type) {
		if (type.isAssignableFrom(Consumer.class)) {
			throw new IllegalArgumentException("Given class does not implement Consumer");
		}
		for (final Consumer<PingResult> c : this.listeners) {
			if (c.getClass() == type) {
				this.listeners.remove(c);
			}
		}
	}

	public List<String> getAddresses() {
		return this.addresses;
	}

	public void pause() {
		this.started = false;
	}

	public void firePings(final Collection<PingResult> col) {
		for (final Consumer<PingResult> cons : this.listeners) {
			for (final PingResult res : col) {
				cons.accept(res);
			}
		}
	}

	public boolean isUseHttps() {
		return this.useHttps;
	}

	public void setUseHttps(final boolean useHttps) {
		this.useHttps = useHttps;
	}

	public long getInterval() {
		return this.interval;
	}

	public void setInterval(final long interval) {
		this.interval = interval;
	}

}
