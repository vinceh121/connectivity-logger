package me.vinceh121.cl.loggers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Consumer;

import me.vinceh121.cl.PingResult;

public abstract class AbstractFileLogger implements Consumer<PingResult> {
	protected FileOutputStream out;
	protected PrintWriter writer;

	public AbstractFileLogger(final String path) throws FileNotFoundException {
		this(new File(path));
	}

	public AbstractFileLogger(final File file) throws FileNotFoundException {
		this.out = new FileOutputStream(file, true);
		this.writer = new PrintWriter(this.out, true);
	}

	public void close() throws IOException {
		this.out.close();
	}
}
