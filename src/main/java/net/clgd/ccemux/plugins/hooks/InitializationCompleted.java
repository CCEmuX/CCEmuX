package net.clgd.ccemux.plugins.hooks;

/**
 * This hook is called when CCEmuX has finished initialization - after CC has
 * been loaded, but before any computers have been created. This hook should be
 * used to perform setup tasks that require interaction with CC code.
 * 
 * @author apemanzilla
 * @see Closing
 * @see net.clgd.ccemux.plugins.Plugin#setup() Plugin.setup()
 */
@FunctionalInterface
public interface InitializationCompleted extends Hook {
	public void onInitializationCompleted();
}
