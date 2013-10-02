package de.frosner.datagenerator.export;

import static org.fest.assertions.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import de.frosner.datagenerator.distributions.DummyDistribution;
import de.frosner.datagenerator.features.DummyFeatureValue;
import de.frosner.datagenerator.features.FeatureDefinition;
import de.frosner.datagenerator.generator.Instance;

public class CsvExportConnectionTest {

	private OutputStream _out = new ByteArrayOutputStream();
	private CsvExportConnection _csvExportConnection;
	private Instance _dummyInstanceWithOneFeature;
	private Instance _dummyInstanceWithTwoFeatures;

	@Before
	public void createCsvExportConnection() {
		_out = new ByteArrayOutputStream();
		_csvExportConnection = new CsvExportConnection(_out, false, false);
		_dummyInstanceWithOneFeature = new Instance(0, new DummyFeatureValue(0));
		_dummyInstanceWithTwoFeatures = new Instance(0, new DummyFeatureValue(0), new DummyFeatureValue(1));
	}

	@Test
	public void testExport_oneInstance_oneFeature() throws IOException {
		_csvExportConnection.exportInstance(_dummyInstanceWithOneFeature);
		_csvExportConnection.close();

		assertThat(_out.toString())
				.isEqualTo(_dummyInstanceWithOneFeature.getFeatureValue(0).getValueAsString() + "\n");
	}

	@Test
	public void testExport_twoInstances_oneFeature() throws IOException {
		_csvExportConnection.exportInstance(_dummyInstanceWithOneFeature);
		_csvExportConnection.exportInstance(_dummyInstanceWithOneFeature);
		_csvExportConnection.close();

		assertThat(_out.toString()).isEqualTo(
				_dummyInstanceWithOneFeature.getFeatureValue(0).getValueAsString() + "\n"
						+ _dummyInstanceWithOneFeature.getFeatureValue(0).getValueAsString() + "\n");
	}

	@Test
	public void testExport_oneInstance_twoFeatures() throws IOException {
		_csvExportConnection.exportInstance(_dummyInstanceWithTwoFeatures);
		_csvExportConnection.close();

		assertThat(_out.toString()).isEqualTo(
				_dummyInstanceWithTwoFeatures.getFeatureValue(0).getValueAsString() + ","
						+ _dummyInstanceWithTwoFeatures.getFeatureValue(1).getValueAsString() + "\n");
	}

	@Test
	public void testExport_twoInstances_twoFeatures() throws IOException {
		_csvExportConnection.exportInstance(_dummyInstanceWithTwoFeatures);
		_csvExportConnection.exportInstance(_dummyInstanceWithTwoFeatures);
		_csvExportConnection.close();

		assertThat(_out.toString()).isEqualTo(
				_dummyInstanceWithTwoFeatures.getFeatureValue(0).getValueAsString() + ","
						+ _dummyInstanceWithTwoFeatures.getFeatureValue(1).getValueAsString() + "\n"
						+ _dummyInstanceWithTwoFeatures.getFeatureValue(0).getValueAsString() + ","
						+ _dummyInstanceWithTwoFeatures.getFeatureValue(1).getValueAsString() + "\n");
	}

	@Test(expected = IOException.class)
	public void testExportAfterClose() throws IOException {
		_csvExportConnection.close();
		_csvExportConnection.exportInstance(_dummyInstanceWithOneFeature);
	}

	@Test
	public void testExportMetaData_oneFeatureName() throws IOException {
		_csvExportConnection = new CsvExportConnection(_out, true, false);
		_csvExportConnection.exportMetaData(Lists.newArrayList(new FeatureDefinition("usheight",
				new DummyDistribution())));
		_csvExportConnection.close();

		assertThat(_out.toString()).isEqualTo("usheight" + "\n");
	}

	@Test
	public void testExportMetaData_twoFeatureNames() throws IOException {
		_csvExportConnection = new CsvExportConnection(_out, true, false);
		_csvExportConnection.exportMetaData(Lists.newArrayList(new FeatureDefinition("usheight",
				new DummyDistribution()), new FeatureDefinition("uswidth", new DummyDistribution())));
		_csvExportConnection.close();

		assertThat(_out.toString()).isEqualTo("usheight" + "," + "uswidth" + "\n");
	}

	@Test
	public void testExportMetaData_doNotExportFeatureNames() throws IOException {
		_csvExportConnection = new CsvExportConnection(_out, false, false);
		_csvExportConnection.exportMetaData(Lists.newArrayList(new FeatureDefinition("usheight",
				new DummyDistribution())));
		_csvExportConnection.close();

		assertThat(_out.toString()).isEmpty();
	}

	@Test
	public void testExportMetaData_exportInstanceIds() throws IOException {
		_csvExportConnection = new CsvExportConnection(_out, false, true);
		_csvExportConnection.exportMetaData(Lists.newArrayList(new FeatureDefinition("test", new DummyDistribution())));
		_csvExportConnection.exportInstance(_dummyInstanceWithTwoFeatures);
		_csvExportConnection.exportInstance(_dummyInstanceWithTwoFeatures);
		_csvExportConnection.close();

		String[] lines = _out.toString().split("\n");
		assertThat(lines[0]).startsWith("0,");
		assertThat(lines[1]).startsWith("0,");
	}

	@Test
	public void testExportMetaData_exportInstanceIds_exportFeatureNames() throws IOException {
		_csvExportConnection = new CsvExportConnection(_out, true, true);
		_csvExportConnection.exportMetaData(Lists.newArrayList(new FeatureDefinition("test", new DummyDistribution())));
		_csvExportConnection.exportInstance(_dummyInstanceWithTwoFeatures);
		_csvExportConnection.exportInstance(_dummyInstanceWithTwoFeatures);
		_csvExportConnection.close();

		String[] lines = _out.toString().split("\n");
		assertThat(lines[0]).startsWith("ID,");
		assertThat(lines[1]).startsWith("0,");
		assertThat(lines[2]).startsWith("0,");
	}

	@Test(expected = MethodNotCallableTwiceException.class)
	public void testExportMetaData_mustNotBeCallableTwice() throws IOException {
		_csvExportConnection = new CsvExportConnection(_out, true, false);
		_csvExportConnection.exportMetaData(Lists.newArrayList(new FeatureDefinition("usheight",
				new DummyDistribution())));
		_csvExportConnection.exportMetaData(Lists.newArrayList(new FeatureDefinition("usheight",
				new DummyDistribution())));
	}

	@Test(expected = IllegalMethodCallSequenceException.class)
	public void testExportMetaData_mustNotBeCallableAfterExportInstance() throws IOException {
		_csvExportConnection = new CsvExportConnection(_out, true, false);
		_csvExportConnection.exportInstance(_dummyInstanceWithOneFeature);
		_csvExportConnection.exportMetaData(Lists.newArrayList(new FeatureDefinition("usheight",
				new DummyDistribution())));
	}

}
