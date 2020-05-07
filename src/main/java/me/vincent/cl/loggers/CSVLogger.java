package me.vincent.cl.loggers;

import java.io.File;
import java.io.FileNotFoundException;

import me.vincent.cl.PingResult;

public class CSVLogger extends AbstractFileLogger {

	public CSVLogger(final String path) throws FileNotFoundException {
		super(path);
	}

	public CSVLogger(final File f) throws FileNotFoundException {
		super(f);
	}

	@Override
	public void accept(final PingResult t) {
		this.writer.print(t.getDate());
		this.writer.print(",");
		this.writer.print(t.getRawAddr());
		this.writer.print(",");
		this.writer.print(t.getAddress());
		this.writer.print(",");
		this.writer.print(t.getHttpStatus());
		this.writer.print(",");
		this.writer.print(t.getHTTPError());
		this.writer.print(",");
		this.writer.print(t.getDnsError());
		this.writer.println();
		this.writer.flush();
	}

}
