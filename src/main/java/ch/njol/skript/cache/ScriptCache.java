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
 * Copyright 2011-2016 Peter Güttinger and contributors
 * 
 */

package ch.njol.skript.cache;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

import com.google.gson.Gson;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import me.nallar.whocalled.WhoCalled;

/**
 * Manages Skript's script cache.
 */
public final class ScriptCache {
	
	private static final int META_LENGTH = 24;
	
	private Path cacheDir;
	private FileChannel metaFile;
	
	private byte secret;
	
	/**
	 * Metadata about Skript's cache.
	 */
	private static class CacheMeta {
		
		/**
		 * When scripts were last cached.
		 */
		Map<String, Long> lastCached = new HashMap<>();
		
		Map<String, Byte> xorKeys = new HashMap<>();
		
		Map<String, Long> xorHashes = new HashMap<>();
	}
	
	private CacheMeta meta;
	
	private Gson gson;
	
	/**
	 * Initializes a new script cache. Only for internal usage.
	 * @throws IOException If something went wrong.
	 */
	@SuppressWarnings("null")
	public ScriptCache(Path cacheDir, int pageSize) throws IOException {
		if (WhoCalled.$.getCallingClass() != Skript.class) {
			throw new SkriptAPIException("script cache is only meant for internal usage, for now");
		}
		
		metaFile = FileChannel.open(cacheDir.resolve("meta.bin"), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
		gson = new Gson();
	}
	
	public void save(CachedScript script, String name, int version) throws IOException {
		String json = script.serialize(gson);
		byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		
		Random rng = new Random();
		byte xorKey = (byte) (rng.nextInt(256) - 128);
		long xorHash = 1;
		
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (bytes[i] ^ xorKey);
			xorHash = 31 * xorHash + bytes[i];
		}
		
		// Delete old file if exists
		Files.deleteIfExists(cacheDir.resolve(name));
		
		try (GZIPOutputStream fos = new GZIPOutputStream(new FileOutputStream(cacheDir.resolve(name).toFile()))) {
			fos.write(bytes);
		}
		
		// Save some metadata
		meta.lastCached.put(name, System.currentTimeMillis());
		meta.xorKeys.put(name, xorKey);
		meta.xorHashes.put(name, xorHash);
	}
	
}