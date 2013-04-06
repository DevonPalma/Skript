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

package ch.njol.skript.classes;

/**
 * @author Peter Güttinger
 */
public class EnumSerializer<T extends Enum<T>> implements Serializer<T> {
	
	private final Class<T> c;
	
	public EnumSerializer(final Class<T> c) {
		this.c = c;
	}
	
	@Override
	public String serialize(final T t) {
		return t.name();
	}
	
	@Override
	public T deserialize(final String s) {
		try {
			return Enum.valueOf(c, s);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public boolean mustSyncDeserialization() {
		return false;
	}
	
}
