package net.clgd.ccemux.api.plugins.hooks;

import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.rendering.Renderer;

/**
 * CCEmuX plugins can register hooks - callbacks that are called when various
 * events occur. This interface is merely a common super interface and contains
 * no members, instead child interfaces should be used from the
 * {@link net.clgd.ccemux.api.plugins.hooks} package.
 *
 * In approximate chronological order, here is a list of some common hooks:
 * <ul>
 * <li>{@link InitializationCompleted} - CCEmuX has finished startup tasks</li>
 * <li>{@link CreatingROM} - CCEmuX is creating the ROM that computers will
 * use</li>
 * <li>{@link CreatingComputer} - An {@link EmulatedComputer} instance is being
 * created</li>
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
 */
public interface Hook {}
