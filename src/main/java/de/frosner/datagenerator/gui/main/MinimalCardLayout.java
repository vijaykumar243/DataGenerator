package de.frosner.datagenerator.gui.main;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;

import de.frosner.datagenerator.util.ApplicationMetaData;

/**
 * {@linkplain CardLayout} adjusting its preferred size to the size of the currently visible card. The normal behavior
 * is to use the maximum size of all cards.
 */
public class MinimalCardLayout extends CardLayout {

	private static final long serialVersionUID = ApplicationMetaData.SERIAL_VERSION_UID;

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		Component current = findCurrentComponent(parent);
		if (current != null) {
			Insets insets = parent.getInsets();
			Dimension pref = current.getPreferredSize();
			pref.width += insets.left + insets.right;
			pref.height += insets.top + insets.bottom;
			return pref;
		}
		return super.preferredLayoutSize(parent);
	}

	public Component findCurrentComponent(Container parent) {
		for (Component comp : parent.getComponents()) {
			if (comp.isVisible()) {
				return comp;
			}
		}
		return null;
	}

}
