package net.clgd.ccemux

import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Display

class EmulatorWindow implements Runnable {
	private static final String EMU_WINDOW_TITLE = "CCEmuX" 
	
	private Shell shell
	private Display display

	new(Display display) {
		this.display = display
		
		shell = new Shell(display)
		shell.text = EMU_WINDOW_TITLE
		
		// TODO: Temporary magic numbers. A 51x19 terminal at 18x27 pixel size fits into this window.
		// Find a better way.
		shell.setSize(918, 513)
		
		// Centre the window.
		val bounds = shell.display.bounds
		val size = shell.size
		
		shell.setBounds(
			(bounds.width - size.x) / 2,
			(bounds.height - size.y) / 2,
			size.x,
			size.y
		)
	}
	
	override void run() {
		shell.open()
		
		// Keeps the window alive and receiving events.
		while (!shell.disposed) {
			if (!display.readAndDispatch) {
				display.sleep
			}
		}
	}
}