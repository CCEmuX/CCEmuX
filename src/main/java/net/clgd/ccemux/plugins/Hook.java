package net.clgd.ccemux.plugins;

import net.clgd.ccemux.emulation.EmulatedComputer;
import net.clgd.ccemux.plugins.hooks.Closing;
import net.clgd.ccemux.plugins.hooks.ComputerBeingCreated;
import net.clgd.ccemux.plugins.hooks.ComputerCreated;
import net.clgd.ccemux.plugins.hooks.ComputerRemoved;
import net.clgd.ccemux.plugins.hooks.InitializationCompleted;
import net.clgd.ccemux.plugins.hooks.RendererCreated;
import net.clgd.ccemux.rendering.Renderer;

/**
 * CCEmuX plugins can register hooks - callbacks that are called when various
 * events occur. This interface is merely a common super interface and contains
 * no members, instead child interfaces should be used from the
 * {@link net.clgd.ccemux.plugins.hooks} package.<br />
 * <br />
 * In approximate chronological order, here is a list of some common hooks:
 * <ul>
 * <li>{@link InitializationCompleted} - CCEmuX has finished startup tasks</li>
 * <li>{@link ComputerBeingCreated} - An {@link EmulatedComputer} instance is
 * being created</li>
 * <li>{@link ComputerCreated} - An {@link EmulatedComputer} instance has been
 * created</li>
 * <li>{@link RendererCreated} - A {@link Renderer} instance has been
 * created</li>
 * <li>{@link ComputerRemoved} - An {@link EmulatedComputer} instance has been
 * removed</li>
 * <li>{@link Closing} - All {@link EmulatedComputer} instances have been
 * removed and CCEmuX is closing</li>
 * </ul>
 * 
 * @author apemanzilla
 *
 */
public interface Hook {

}
