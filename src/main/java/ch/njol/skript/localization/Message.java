/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.localization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import ch.njol.skript.Skript;

/**
 * Basic class to get text from the language file(s).
 * <p>
 * TODO generate warnings for missing english values
 * 
 * @author Peter Güttinger
 */
public class Message {
	
	// this is most likely faster than registering a listener for each Message
	private final static Collection<Message> messages = new ArrayList<Message>(50);
	private static boolean firstChange = true;
	static {
		Language.addListener(new LanguageChangeListener() {
			@Override
			public void onLanguageChange() {
				for (final Message m : messages) {
					m.revalidate = true;
					if (firstChange) {
						if (m.value == null)
							Skript.error("Missing entry '" + m.key + "' in the default english language file!");
					}
				}
				firstChange = false;
			}
		});
	}
	
	public final String key;
	private String value;
	private boolean revalidate = true;
	
	public Message(final String key) {
		this.key = key.toLowerCase(Locale.ENGLISH);
		messages.add(this);
//		if (!Language.english.isEmpty()) {
//			validate();
//			if (value == null)
//				Skript.warning("Missing entry '" + key + "' in the default english language file!");
//		}
	}
	
	@Override
	public String toString() {
		validate();
		return value == null ? key : value;
	}
	
	/**
	 * Gets the text this Message refers to. This method automatically revalidates the value if necessary.
	 * 
	 * @return This message's value or null if it doesn't exist.
	 */
	protected final String getValue() {
		validate();
		return value;
	}
	
	/**
	 * Checks whether this value is set in the current language or the english default.
	 * 
	 * @return
	 */
	public final boolean isSet() {
		validate();
		return value != null;
	}
	
	/**
	 * Checks whether this message's value has changed and calls {@link #onValueChange()} if neccessary.
	 */
	protected void validate() {
		if (revalidate) {
			revalidate = false;
			value = Language.get_(key);
			onValueChange();
		}
	}
	
	/**
	 * Called when this Message's value changes. This is not neccessarily called for every language change, but only when the value is actually accessed and the language has
	 * changed since the last call of this method.
	 */
	protected void onValueChange() {}
	
}
