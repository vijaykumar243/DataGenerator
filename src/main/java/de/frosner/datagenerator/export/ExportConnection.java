package de.frosner.datagenerator.export;

import java.util.List;

import javax.annotation.Nonnull;

import net.sf.qualitycheck.Check;
import net.sf.qualitycheck.exception.IllegalEmptyArgumentException;
import de.frosner.datagenerator.features.FeatureDefinition;
import de.frosner.datagenerator.generator.DataGenerator;
import de.frosner.datagenerator.generator.Instance;

/**
 * Interface for connections that are able to export {@link Instance}s generated by the {@link DataGenerator#generate()}
 * method. The {@link DataGenerator} calls {@link ExportConnection#exportInstanceStrategy(Instance)} for every
 * {@link Instance} generated. {@link ExportConnection#close()} must be called after the last instance has been
 * exported.
 * <p>
 * Export connections should save the feature values in some way and handle buffering on their own.
 */
public abstract class ExportConnection {

	private boolean _metaDataAlreadyExported = false;
	private boolean _alreadyInstancesExported = false;

	/**
	 * Calling this method will send the supplied feature definitions to the {@link ExportConnection}. Depending on the
	 * concrete connection and its configuration, meta data may be exported. This can include something like printing
	 * column names or creating schema definitions.
	 * <p>
	 * Call this method before calling {@link ExportConnection#exportInstanceStrategy(Instance)}. Only call it once.
	 * 
	 * @param featureDefinitions
	 * 
	 * @throws MethodNotCallableTwiceException
	 *             Will be thrown if this method is called twice on this connection. Calling this method more than one
	 *             time is forbidden as it may destroy the export format.
	 */
	public void exportMetaData(@Nonnull List<FeatureDefinition> featureDefinitions) {
		Check.stateIsTrue(!_alreadyInstancesExported, IllegalMethodCallSequenceException.class);
		Check.stateIsTrue(!_metaDataAlreadyExported, MethodNotCallableTwiceException.class);
		_metaDataAlreadyExported = true;

		Check.notEmpty(featureDefinitions);
		Check.noNullElements(featureDefinitions);
		exportMetaDataStrategy(featureDefinitions);
	}

	protected abstract void exportMetaDataStrategy(List<FeatureDefinition> featureDefinitions);

	/**
	 * Calling this method will export the supplied {@link Instance} to the {@link ExportConnection}. Export connections
	 * should handle buffering and other low level operations.
	 * 
	 * @param instance
	 *            to export
	 */
	public void exportInstance(@Nonnull Instance instance) {
		_alreadyInstancesExported = true;

		Check.stateIsTrue(instance.iterator().hasNext(), IllegalEmptyArgumentException.class);
		exportInstanceStrategy(instance);
	}

	protected abstract void exportInstanceStrategy(Instance instance);

	/**
	 * Closes this stream and releases any system resources associated with it. If the stream is already closed then
	 * invoking this method has no effect.
	 */
	public abstract void close();

	/**
	 * Returns the textual representation of the export location. If the connection exports to a file, this could be the
	 * file name.
	 * 
	 * @return textual representation of the export location
	 */
	public abstract String getExportLocation();

}
