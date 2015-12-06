package gui;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import processing.abstracts.WorkflowAbstract;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import core.tags.Tag;
import org.eclipse.swt.custom.TableCursor;



public class userinterface {

	private WorkflowAbstract work = new WorkflowAbstract();
	ArrayList<TableItem> data = new ArrayList<TableItem>();
	
	protected Shell shell;
	private Table table;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			userinterface window = new userinterface();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(788, 523);
		shell.setText("SWT Application");
		
		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				work.init();
				table.setData(work.tags);
				
				for(Tag t: work.tags)
				{
					TableItem tableItem = new TableItem(table, SWT.NONE);
					data.add(tableItem);
					tableItem.setText(t.getTagName());
				}
			}
		});
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnNewButton.setBounds(32, 83, 94, 28);
		btnNewButton.setText("Init");
		
		TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder.setBounds(290, 10, 488, 481);
		
		TabItem tbtmStep = new TabItem(tabFolder, SWT.NONE);
		tbtmStep.setText("Step 1");
		
		TabItem tbtmStep_1 = new TabItem(tabFolder, SWT.NONE);
		tbtmStep_1.setText("Step 2");
		
		TabItem tbtmStep_2 = new TabItem(tabFolder, SWT.NONE);
		tbtmStep_2.setText("Step 3");
		
		table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table.setBounds(31, 197, 228, 140);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableColumn tblclmnTagName = new TableColumn(table, SWT.NONE);
		tblclmnTagName.setWidth(500);
		tblclmnTagName.setText("Abstract");
		
		TableCursor tableCursor = new TableCursor(table, SWT.NONE);
		
	}
}
