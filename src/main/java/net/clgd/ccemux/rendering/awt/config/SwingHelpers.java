package net.clgd.ccemux.rendering.awt.config;

import java.awt.Insets;
import java.util.Optional;

import javax.swing.JComponent;

import com.google.common.html.HtmlEscapers;

public class SwingHelpers {
	public static final Insets PADDING = new Insets(5, 5, 5, 5);
	public static final Insets SMALL_PADDING = new Insets(2, 2, 2, 2);

	public static void hideBackground(JComponent component) {
		component.setOpaque(false);
		component.setBackground(null);
		component.setBorder(null);
	}

	public static void setTooltip(JComponent component, Optional<String> tooltip) {
		if (tooltip.isPresent()) {
			component.setToolTipText("<html>" + HtmlEscapers.htmlEscaper().escape(tooltip.get()).replace("\n", "<br />") + "</html>");
		} else {
			component.setToolTipText(null);
		}
	}
}
