package de.frosner.datagenerator.export;

import java.io.Closeable;
import java.io.IOException;

import de.frosner.datagenerator.generator.DataGenerator;
import de.frosner.datagenerator.generator.Instance;

/**
 * Interface for connections that are able to export {@link Instance}s generated by the {@link DataGenerator#generate()}
 * method. The {@link DataGenerator} calls {@link ExportConnection#export(Instance)} for every {@link Instance}
 * generated. {@link ExportConnection#close()} must be called after the last instance has been exported.
 * <p>
 * Export connections should save the feature values in some way and handle buffering on their own.
 */
public interface ExportConnection extends Closeable {

	/**
	 * Calling this method will export the supplied {@link Instance} to the {@link ExportConnection}. Export connections
	 * should handle buffering and other low level operations.
	 * 
	 * @param instance
	 *            to export
	 * @throws IOException
	 */
	public void export(Instance instance) throws IOException;

}
