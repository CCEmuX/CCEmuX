package net.clgd.ccemux.rendering;

import net.clgd.ccemux.init.Config;

/**
 * Contains fields that may be used by {@link RendererFactory}
 * @author apemanzilla
 *
 */
public class RendererConfig {
	public final double termScale;
	
	public RendererConfig(double termScale) {
		this.termScale = termScale;
	}
	
	public RendererConfig(Config generalConfig) {
		this(generalConfig.getTermScale());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(termScale);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		RendererConfig other = (RendererConfig) obj;
		if (Double.doubleToLongBits(termScale) != Double.doubleToLongBits(other.termScale)) return false;
		return true;
	}
}
