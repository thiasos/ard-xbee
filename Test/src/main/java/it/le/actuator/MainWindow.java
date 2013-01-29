package it.le.actuator;

import java.awt.EventQueue;

import javax.swing.JFrame;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import com.rapplogic.xbee.api.wpan.RxResponse16;
import com.rapplogic.xbee.api.wpan.TxRequest64;

import javax.swing.JButton;
import java.awt.BorderLayout;
import javax.swing.JProgressBar;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JLabel;

public class MainWindow implements PacketListener {

	private JFrame frmControlloCupola;
	private XBee xbee;
	private final Action actionOpen = new SwingAction(5, "Apri");
	private final Action actionClose = new SwingAction(6, "Chiudi");
	private JButton btnOpen;
	private JButton btnClose;
	private JProgressBar progressBar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frmControlloCupola.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 * @throws Exception
	 */
	public MainWindow() throws Exception {
		initialize();
		this.xbee = new XBee();
		xbee.open("/dev/tty.usbserial-A600KLBT", 9600);
		xbee.addPacketListener(this);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmControlloCupola = new JFrame();
		frmControlloCupola.setTitle("Controllo Cupola");
		frmControlloCupola.setBounds(100, 100, 748, 81);
		frmControlloCupola.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.btnOpen = new JButton("Apri");
		btnOpen.setAction(actionOpen);

		frmControlloCupola.getContentPane().add(btnOpen, BorderLayout.WEST);

		this.btnClose = new JButton("Chiudi");
		btnClose.setAction(actionClose);
		frmControlloCupola.getContentPane().add(btnClose, BorderLayout.EAST);
		
		panel = new JPanel();
		frmControlloCupola.getContentPane().add(panel, BorderLayout.CENTER);
				panel.setLayout(new BorderLayout(0, 0));
		
				this.progressBar = new JProgressBar();
				panel.add(progressBar, BorderLayout.CENTER);
				progressBar.setMaximum(10);
				
				lblNewLabel = new JLabel("New label");
				panel.add(lblNewLabel, BorderLayout.SOUTH);
				progressBar.setVisible(false);
	}

	@Override
	public void processResponse(XBeeResponse arg0) {

		if (arg0 instanceof RxResponse16) {
			RxResponse16 rxres = (RxResponse16) arg0;
			StringWriter sw = new StringWriter();
			int[] data = rxres.getData();

			for (int i : data) {
				sw.write(i);
			}
			String[] string = sw.toString().split("/");

			for (final String idx : string) {
				if (idx.length() > 1) {
					EventQueue.invokeLater(new Runnable() {

						@Override
						public void run() {
							btnOpen.setEnabled(idx.charAt(1) == 'L');
							btnClose.setEnabled(idx.charAt(1) == 'L');
							progressBar.setVisible(idx.charAt(1) == 'R');
							if (idx.charAt(1) == 'R') {
								progressBar.setValue(idx.charAt(2) - 48);
							}
						}
					});

				}
			}
		}
	}

	private class SwingAction extends AbstractAction {
		private final int pin;

		public SwingAction(int pin, String name) {
			putValue(ACTION_COMMAND_KEY, "");
			this.pin = pin;
			putValue(NAME, name);
			putValue(SHORT_DESCRIPTION, "Some short description");

		}

		public void actionPerformed(ActionEvent e) {
			try {
				switchPin(pin);
			} catch (XBeeTimeoutException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (XBeeException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	static XBeeAddress64 remoteAddress = new XBeeAddress64(0x00, 0x13, 0xa2, 0x00, 0x40, 0x81, 0xb8, 0xe5);
	private JPanel panel;
	private JLabel lblNewLabel;

	private void switchPin(int pin) throws XBeeTimeoutException, XBeeException {
		int[] buffer = { pin };
		TxRequest64 req = new TxRequest64(remoteAddress, buffer);
		xbee.sendSynchronous(req, 5000);
	}

}
