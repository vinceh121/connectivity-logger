package me.vinceh121.cl.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import com.formdev.flatlaf.FlatDarkLaf;

import me.vinceh121.cl.ConnectivityPinger;
import me.vinceh121.cl.loggers.CSVLogger;

public class CLGui extends JFrame {
	private static final long serialVersionUID = 1L;
	private final ConnectivityPinger cn;
	private ImageIcon imgIcon, imgIconPing, imgIconError;
	private JList<String> listHost;
	private TrayIcon trayIcon;
	private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

	public static void main(final String[] args) {
		if (!FlatDarkLaf.install()) {
			JOptionPane.showMessageDialog(null, "Failed to install FlatDarkLaf. App is gonna be ugly metal");
		}

		final CLGui gui = new CLGui();
		gui.pack();
		gui.setVisible(true);
	}

	public CLGui() {
		this.cn = new ConnectivityPinger();

		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.setTitle("Connectivity Logger");

		this.setupImages();
		this.setupTray();
		this.setupContent();
		this.setupBar();

		this.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(final WindowEvent e) {
			}

			@Override
			public void windowIconified(final WindowEvent e) {
			}

			@Override
			public void windowDeiconified(final WindowEvent e) {
			}

			@Override
			public void windowDeactivated(final WindowEvent e) {
			}

			@Override
			public void windowClosing(final WindowEvent e) {
				CLGui.this.trayIcon.displayMessage("Connectivity Logger in background",
						"Connectivity Logger is still running in the background. "
								+ "To close it, use the context menu.",
						MessageType.INFO);
			}

			@Override
			public void windowClosed(final WindowEvent e) {
			}

			@Override
			public void windowActivated(final WindowEvent e) {
			}
		});

		this.cn.addListener(r -> {
			if (r.getHTTPError() != null || r.getDnsError() != null) {
				flashTrayIcon(imgIconError);
			} else {
				flashTrayIcon(imgIconPing);
			}
		});
	}

	private void setupImages() {
		this.imgIcon = new ImageIcon(CLGui.class.getResource("/icons/transmit.png"));
		this.imgIconPing = new ImageIcon(CLGui.class.getResource("/icons/transmit_go.png"));
		this.imgIconError = new ImageIcon(CLGui.class.getResource("/icons/transmit_error.png"));

		this.setIconImage(this.imgIcon.getImage());
	}

	private void setupTray() {
		if (!SystemTray.isSupported()) {
			JOptionPane.showMessageDialog(null,
					"The current environment doesn't support icon trays. "
							+ "The app will exit upon closing the window.");
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}

		final SystemTray sysTray = SystemTray.getSystemTray();
		this.trayIcon = new TrayIcon(this.imgIcon.getImage(), "Connectivity Logger");
		try {
			sysTray.add(this.trayIcon);
		} catch (final AWTException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to add tray icon: " + e.toString());
			return;
		}

		final PopupMenu popup = new PopupMenu("Connectivity Logger");

		final MenuItem mntShow = new MenuItem("Show window");
		mntShow.addActionListener(e -> {
			this.setVisible(true);
		});
		popup.add(mntShow);

		final MenuItem mntExit = new MenuItem("Exit");
		mntExit.addActionListener(e -> System.exit(0));
		popup.add(mntExit);

		this.trayIcon.setPopupMenu(popup);
	}

	private void setupContent() {
		final JTabbedPane tabbed = new JTabbedPane();
		this.setContentPane(tabbed);

		final JPanel pnList = new JPanel();
		tabbed.addTab("Hosts", new ImageIcon(CLGui.class.getResource("/icons/server.png")), pnList);

		pnList.setLayout(new BorderLayout());

		this.listHost = new JList<>();
		pnList.add(this.listHost, BorderLayout.CENTER);

		final JPanel pnListBtn = new JPanel();

		final JButton btnAddHost
				= new JButton("Add host", new ImageIcon(CLGui.class.getResource("/icons/server_add.png")));
		btnAddHost.addActionListener(e -> {
			final String host = JOptionPane.showInputDialog("Host:");
			System.out.println("Added host: " + host);
			this.cn.getAddresses().add(host);
			this.updateHostList();
		});
		pnListBtn.add(btnAddHost);

		final JButton btnRemoveHost
				= new JButton("Remove host", new ImageIcon(CLGui.class.getResource("/icons/server_delete.png")));
		btnRemoveHost.addActionListener(e -> {
			this.cn.getAddresses().removeAll(this.listHost.getSelectedValuesList());
			this.updateHostList();
		});
		pnListBtn.add(btnRemoveHost);

		pnList.add(pnListBtn, BorderLayout.NORTH);

		final CLGraph liveGraph = new CLGraph("Live Graph");
		this.cn.addListener(liveGraph);
		tabbed.addTab("Live Graph", new ImageIcon(CLGui.class.getResource("/icons/chart_line.png")), liveGraph);
	}

	private void updateHostList() {
		this.listHost.setModel(new MyListModel(this.cn));
	}

	private void setupBar() {
		final JMenuBar bar = new JMenuBar();
		this.setJMenuBar(bar);

		final JMenu mnFile = new JMenu("File");
		bar.add(mnFile);

		final JMenu mnSubOpen = new JMenu("Open...");
		mnFile.add(mnSubOpen);

		final JMenuItem mntOpenCsv = new JMenuItem("...CSV");
		mnSubOpen.add(mntOpenCsv);

		final JMenu mnLogTo = new JMenu("Log to...");
		mnFile.add(mnLogTo);

		final JCheckBoxMenuItem mntLogCsv = new JCheckBoxMenuItem("...CSV");
		mntLogCsv.addActionListener(e -> {
			if (mntLogCsv.isSelected()) {
				final JFileChooser fc = new JFileChooser();
				final int status = fc.showSaveDialog(null);
				if (status != JFileChooser.APPROVE_OPTION) {
					JOptionPane.showMessageDialog(null, "Not gonna log");
					return;
				}
				CSVLogger logger;
				try {
					logger = new CSVLogger(fc.getSelectedFile());
				} catch (final FileNotFoundException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null, e1.toString());
					return;
				}
				this.cn.addListener(logger);
			} else {
				this.cn.removeListenerType(CSVLogger.class);
			}
		});
		mnLogTo.add(mntLogCsv);

		mnFile.addSeparator();

		final JMenuItem mntExit = new JMenuItem("Exit");
		mntExit.addActionListener(e -> System.exit(0));
		mnFile.add(mntExit);

		final JMenu mnPinger = new JMenu("Pinger");
		bar.add(mnPinger);

		final JCheckBoxMenuItem mntRunning = new JCheckBoxMenuItem("Running");
		mntRunning.addActionListener(e -> {
			if (mntRunning.isSelected()) {
				this.cn.start();
			} else {
				this.cn.pause();
			}
		});
		mnPinger.add(mntRunning);

		final JMenuItem mntForcePing = new JMenuItem("Force ping");
		mntForcePing.addActionListener(e -> EventQueue.invokeLater(() -> this.cn.firePings(this.cn.pingAll())));
		mnPinger.add(mntForcePing);

		final JMenu mnInterval = new JMenu("Ping interval");
		mnPinger.add(mnInterval);

		final JMenuItem mntTime = new JMenuItem("Interval: " + this.cn.getInterval());
		mntTime.addActionListener(e -> {
			try {
				this.cn.setInterval(this.timeUnit
						.toMillis(Long.parseLong(JOptionPane.showInputDialog("Interval in " + this.timeUnit))));
				mntTime.setText("Interval: " + this.cn.getInterval());
			} catch (final NumberFormatException ex) {}
		});
		mnInterval.add(mntTime);

		final JMenuItem mntTimeUnit = new JMenuItem("Unit: " + this.timeUnit);
		mntTimeUnit.addActionListener(e -> {
			final int select = JOptionPane.showOptionDialog(null,
					"Choose a time unit",
					"Time unit",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					this.imgIcon,
					TimeUnit.values(),
					this.timeUnit);

			if (select == JOptionPane.CLOSED_OPTION) {
				return;
			} else {
				this.timeUnit = TimeUnit.values()[select];
				this.cn.setInterval(this.timeUnit.toMillis(this.cn.getInterval()));
				mntTimeUnit.setText("Unit: " + this.timeUnit);
			}
		});
		mnInterval.add(mntTimeUnit);
	}

	private void flashTrayIcon(final ImageIcon img) {
		new Thread(() -> {
			trayIcon.setImage(img.getImage());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			trayIcon.setImage(imgIcon.getImage());
		}, "TrayIconFlasher").start();
	}

	class MyListModel extends AbstractListModel<String> {
		private static final long serialVersionUID = -8493001290085624110L;
		private ConnectivityPinger cn;

		public MyListModel(final ConnectivityPinger cn) {
			this.cn = cn;
		}

		@Override
		public int getSize() {
			return this.cn.getAddresses().size();
		}

		@Override
		public String getElementAt(final int index) {
			return this.cn.getAddresses().get(index);
		}

	}
}
