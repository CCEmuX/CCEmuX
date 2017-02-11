package net.clgd.ccemux.rendering;

import net.clgd.ccemux.init.CCEmuXConfig;

/**
 * Contains fields that may be used by {@link RendererFactory}
 * @author apemanzilla
 *
 */
public class RendererConfig {
	public final int termScale;
	
	public RendererConfig(int termScale) {
		this.termScale = termScale;
	}
	
	public RendererConfig(CCEmuXConfig generalConfig) {
		this(generalConfig.getTermScale());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + termScale;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		RendererConfig other = (RendererConfig) obj;
		if (termScale != other.termScale) return false;
		return true;
	}
}
