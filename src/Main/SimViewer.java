package Main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

@SuppressWarnings("serial")
public class SimViewer extends JPanel {
	JFileChooser fc;
	DefaultTableModel tableModel;

	int colourDivideValue = 50;
	int columnCount;
	String windowTitle;
	
	String[] columnNames;
	String[][] resultsList;

	public void displayFileChooser() {
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		// Allow only text files
		FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt");
		fc.setFileFilter(filter);

		int returnVal = fc.showOpenDialog(SimViewer.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Path path = Paths.get(fc.getSelectedFile().getAbsolutePath());
			windowTitle = fc.getSelectedFile().getAbsolutePath();
			try {				
				parseFile(path);
			} catch (IOException e) {
				System.err.println(e);
			}
			displayTable();
		}
	}

	public void parseFile(Path path) throws IOException {
		// Number of programs
		List<String> fileLines = Files.readAllLines(path);
		int maxP = Integer.parseInt(fileLines.get(0));

		columnNames = new String[maxP + 1];
		resultsList = new String[maxP][maxP + 1];

		// String array of column labels
		columnNames = fileLines.get(1).split(" ");
		columnCount = columnNames.length;

		for (int i = 0; i < maxP; i++) {
			String line = fileLines.get(i + 2);
			int j = 0;
			for (String value : line.split(" ")) {
				resultsList[i][j] = value;
				j++;
			}
		}
	}

	private void displayTable() {
		JFrame frame = new JFrame(windowTitle);
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// JPanel that handles graphs
		JPanel tablePanel = new JPanel(new GridLayout(0, 1));
		tablePanel.setMinimumSize(new Dimension(600, 600));

		JTable tableHolder = getNewRenderedTable(getTables(), colourDivideValue, columnCount);

		JScrollPane tableScrollPanel = new JScrollPane(tableHolder, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		tablePanel.add(tableScrollPanel);

		// JPanel that handles the buttons
		JPanel buttonPanel = new JPanel(new FlowLayout());
		SpinnerModel sm = new SpinnerNumberModel(50, 0, 98, 2);
		JSpinner colourDivide = new JSpinner(sm);
		colourDivide.setToolTipText("<html>Set upper bound for<br>low similarity colour</html>");

		JFormattedTextField tf = ((JSpinner.DefaultEditor) colourDivide.getEditor()).getTextField();
		tf.setEditable(false);
		tf.setBackground(Color.white);

		JLabel colorLable = new JLabel("<html>Colouring Boundary<br>(0 for no colouring)</html>");
		JLabel space1 = new JLabel("   ");
		JLabel space2 = new JLabel("   ");

		JButton updateColor = new JButton("Update");
		updateColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				colourDivideValue = (int) colourDivide.getValue();
				tableScrollPanel.setViewportView(getNewRenderedTable(getTables(), colourDivideValue, columnCount));
			}
		});

		buttonPanel.add(colorLable);
		buttonPanel.add(space1);
		buttonPanel.add(colourDivide);
		buttonPanel.add(space2);
		buttonPanel.add(updateColor);

		frame.add(tableScrollPanel, BorderLayout.CENTER);
		frame.add(buttonPanel, BorderLayout.PAGE_START);

		frame.setSize(columnCount*30, columnCount*23);
		frame.setMinimumSize(new Dimension(300, 200));
		frame.setVisible(true);
		frame.validate();

	}

	public JTable getTables() {
		tableModel = new DefaultTableModel(resultsList, columnNames) {
			public boolean isCellEditable(int row, int column) {
				return false;// this makes the cells not editable
			}
		};
		JTable dataTable = new JTable(tableModel) {
			@Override
			public Dimension getPreferredScrollableViewportSize() {
				return new Dimension(columnCount*29, columnCount*22);
			}
			public boolean getScrollableTracksViewportWidth() {
				return getPreferredSize().width < getParent().getWidth();
			}
		};

		dataTable.getTableHeader().setReorderingAllowed(false);
		
		return dataTable;
	}

	public static JTable getNewRenderedTable(final JTable table, int lowPart, int columns) {
		table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int col) {
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
				String cellV = (String) table.getModel().getValueAt(row, col);

				Color labelGray = new Color(200, 200, 200);

				table.isCellEditable(row, col);
				table.setFocusable(false);
				table.setRowSelectionAllowed(false);
				table.getTableHeader().setOpaque(false);
				table.getTableHeader().setBackground(labelGray);
				
				TableColumn column = null;
				
				DefaultTableCellRenderer centreRenderer = new DefaultTableCellRenderer();
				centreRenderer.setHorizontalAlignment(SwingConstants.CENTER);
				setHorizontalAlignment(SwingConstants.RIGHT);

				for (int i = 0; i < columns; i++) {
					column = table.getColumnModel().getColumn(i);
					column.setPreferredWidth(26);
					if(i == 0) {  
						column.setCellRenderer(centreRenderer);			
						column.setPreferredWidth(30);
						centreRenderer.setBackground(labelGray);
					}
				}

				int highPart = (lowPart + 100) / 2;

				if (col == 0) {
					centreRenderer.setBackground(labelGray);
					centreRenderer.setForeground(Color.black);
					return this;
				}
				
				if (cellV == null || lowPart == 0) {
					setBackground(Color.white);
					setForeground(Color.black);
					return this;
				} else {
					try {
						if (Double.parseDouble(cellV) <= lowPart) {
							setBackground(Color.green);
						} else if (Double.parseDouble(cellV) > lowPart && Double.parseDouble(cellV) <= highPart) {
							setBackground(Color.yellow);
						} else if (Double.parseDouble(cellV) > highPart) {
							setBackground(Color.red);
						} else {
							setBackground(table.getBackground());
							setForeground(table.getForeground());
						}
					} catch (NumberFormatException ex) {
						System.err.println(ex);
					}
				}
				return this;
			}
		});
		return table;
	}

	public static void main(String[] args) {
		SimViewer sv = new SimViewer();
		sv.displayFileChooser();
	}
}