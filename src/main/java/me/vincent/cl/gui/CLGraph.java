package me.vincent.cl.gui;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import me.vincent.cl.PingResult;

public class CLGraph extends JPanel implements Consumer<PingResult> {
	private static final long serialVersionUID = -712940644358855573L;
	public static final String[] SYMBOLS
			= new String[] { "Connection Error", "DNS Error", "Negative HTTP status", "Redirect", "Healthy" };
	public static final Collection<Integer> HEALTHY_STATUSES = Arrays.asList(100, 101, 200, 202, 204),
			REDIRECT_STATUSES = Arrays.asList(301, 302, 303, 307, 308);
	private final ChartPanel panel;
	private final TimeSeriesCollection dataset;
	private final JFreeChart chart;

	public CLGraph(final String title) {
		this.dataset = new TimeSeriesCollection();
		this.chart = this.createTimeSeriesChart(title, "Time", "Connectivity state", this.dataset);
		this.panel = new ChartPanel(this.chart);

		StandardChartTheme.createDarknessTheme().apply(this.chart);

		this.setLayout(new BorderLayout());
		this.add(this.panel, BorderLayout.CENTER);
	}

	@Override
	public void accept(final PingResult t) {
		System.out.println("Graph ping: " + t);
		this.getOrCreateSeries(t.getRawAddr()).add(new Second(), this.getSymbol(t));
	}

	private int getSymbol(final PingResult p) {
		if (p.getHTTPError() != null) {
			return 0;
		} else if (p.getDnsError() != null) {
			return 1;
		} else if (CLGraph.REDIRECT_STATUSES.contains(p.getHttpStatus())) {
			return 3;
		} else if (!CLGraph.HEALTHY_STATUSES.contains(p.getHttpStatus())) {
			return 2;
		} else {
			return 4;
		}
	}

	private JFreeChart createTimeSeriesChart(final String title, final String timeAxisLabel,
			final String valueAxisLabel, final XYDataset dataset) {
		final ValueAxis timeAxis = new DateAxis(timeAxisLabel);
		timeAxis.setLowerMargin(0.02); // reduce the default margins
		timeAxis.setUpperMargin(0.02);
		final NumberAxis valueAxis = new SymbolAxis(valueAxisLabel, CLGraph.SYMBOLS);
		valueAxis.setAutoRangeIncludesZero(false); // override default
		final XYPlot plot = new XYPlot(dataset, timeAxis, valueAxis, null);

		final XYToolTipGenerator toolTipGenerator = StandardXYToolTipGenerator.getTimeSeriesInstance();

		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
		renderer.setDefaultToolTipGenerator(toolTipGenerator);
		plot.setRenderer(renderer);

		final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		return chart;
	}

	private TimeSeries getOrCreateSeries(final Comparable<?> key) {
		final TimeSeries ser = this.dataset.getSeries(key);
		if (ser == null) {
			final TimeSeries newSer = new TimeSeries(key);
			this.dataset.addSeries(newSer);
			return newSer;
		} else {
			return ser;
		}
	}

}
