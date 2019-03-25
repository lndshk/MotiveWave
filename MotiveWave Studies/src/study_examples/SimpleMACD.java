package study_examples;

import com.motivewave.platform.sdk.common.*;
import com.motivewave.platform.sdk.common.desc.*;
import com.motivewave.platform.sdk.study.*;

/** Simple MACD example.  This example shows how to create a Study Graph
    that is based on the MACD study.  For simplicity code from the 
    MotiveWave MACD study has been removed or altered. */
@StudyHeader(
    namespace="com.mycompany", 
    id="SimpleMACD", 
    name="Simple MACD",
    desc="This is a simple version of the <b>MACD</b> for example purposes.",
    menu="Examples",
    overlay=false)
public class SimpleMACD extends Study 
{
  // This enumeration defines the variables that we are going to store in the Data Series
	enum Values { MACD, SIGNAL, HIST };
  final static String HIST_IND = "histInd"; // Histogram Parameter 
	
  /** This method initializes the settings and defines the runtime settings. */
  @Override
  public void initialize(Defaults defaults)
  {
    // Define the settings for this study
    // We are creating 2 tabs: 'General' and 'Display'
    SettingsDescriptor settings = new SettingsDescriptor();
    setSettingsDescriptor(settings);
    SettingTab tab = new SettingTab("General");
    settings.addTab(tab);

    // Define the 'Inputs'
    SettingGroup inputs = new SettingGroup("Inputs");
    inputs.addRow(new InputDescriptor(Inputs.INPUT, "Input", Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, "Period 1", 12, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD2, "Period 2", 26, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.SIGNAL_PERIOD, "Signal Period", 
                  9, 1, 9999, 1));
    tab.addGroup(inputs);
    
    tab = new SettingTab("Display");
    settings.addTab(tab);
    // Allow the user to configure the settings for the paths and the histogram
    SettingGroup paths = new SettingGroup("Paths");
    tab.addGroup(paths);
    paths.addRow(new PathDescriptor(Inputs.PATH, "MACD Path", 
                 defaults.getLineColor(), 1.5f, null, true, false, true));
    paths.addRow(new PathDescriptor(Inputs.SIGNAL_PATH, "Signal Path", 
                 defaults.getRed(), 1.0f, null, true, false, true));
    paths.addRow(new BarDescriptor(Inputs.BAR, "Bar Color", 
                 defaults.getBarColor(), true, true));
    // Allow the user to display and configure indicators on the vertical axis
    SettingGroup indicators = new SettingGroup("Indicators");
    tab.addGroup(indicators);
    indicators.addRow(new IndicatorDescriptor(Inputs.IND, "MACD Ind", 
                      null, null, false, true, true));
    indicators.addRow(new IndicatorDescriptor(Inputs.SIGNAL_IND, "Signal Ind", 
                      defaults.getRed(), null, false, false, true));
    indicators.addRow(new IndicatorDescriptor(HIST_IND, "Hist Ind", 
                      defaults.getBarColor(), null, false, false, true));

    RuntimeDescriptor desc = new RuntimeDescriptor();
    setRuntimeDescriptor(desc);
    desc.setMinTick(0.0001);
    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD, 
                          Inputs.PERIOD2, Inputs.SIGNAL_PERIOD);
    // We are exporting 3 values: MACD, SIGNAL and HIST (histogram)
    desc.exportValue(new ValueDescriptor(Values.MACD, "MACD", new String[] 
                     {Inputs.INPUT, Inputs.PERIOD, Inputs.PERIOD2}));
    desc.exportValue(new ValueDescriptor(Values.SIGNAL, "MACD Signal", 
                     new String[] {Inputs.SIGNAL_PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.HIST, "MACD Histogram", new String[] 
                     {Inputs.PERIOD, Inputs.PERIOD2, Inputs.SIGNAL_PERIOD}));
    // There are two paths, the MACD path and the Signal path
    desc.declarePath(Values.MACD, Inputs.PATH);
    desc.declarePath(Values.SIGNAL, Inputs.SIGNAL_PATH);
    // Bars displayed as the histogram
    desc.declareBars(Values.HIST, Inputs.BAR);
    // These are the indicators that are displayed in the vertical axis
    desc.declareIndicator(Values.MACD, Inputs.IND);
    desc.declareIndicator(Values.SIGNAL, Inputs.SIGNAL_IND);
    desc.declareIndicator(Values.HIST, HIST_IND);

    // These variables are used to define the range of the vertical axis
    desc.setRangeKeys(Values.MACD, Values.SIGNAL, Values.HIST);
    // Display a 'Zero' line that is dashed.
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3,3}));
  }

  /** This method calculates the MACD values for the data at the given index. */
  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    int period1 = getSettings().getInteger(Inputs.PERIOD);
    int period2 = getSettings().getInteger(Inputs.PERIOD2);
    int period = Util.max(period1, period2);
    if (index < period) return; // not enough data to compute the MAs

    // MACD is the difference between two moving averages.
    // In our case we are going to use an exponential moving average (EMA)
    Object input = getSettings().getInput(Inputs.INPUT);
    DataSeries series = ctx.getDataSeries();
    Double MA1 = null, MA2 = null;
    
    MA1 = series.ema(index, period1, input);
    MA2 = series.ema(index, period2, input);
    if (MA1 == null || MA2 == null) return;

    // Define the MACD value for this index
    double MACD = MA1 - MA2; 
    //debug("Setting MACD value for index: " + index + " MACD: " + MACD);
    series.setDouble(index, Values.MACD, MACD);

    int signalPeriod = getSettings().getInteger(Inputs.SIGNAL_PERIOD);
    if (index < period + signalPeriod) return; // Not enough data yet

    // Calculate moving average of MACD (signal path)
    Double signal = series.sma(index, signalPeriod, Values.MACD);
    series.setDouble(index, Values.SIGNAL, signal);
    if (signal == null) return;

    // Histogram is the difference between the MACD and the signal path
    series.setDouble(index, Values.HIST, MACD - signal);
    series.setComplete(index);
  }  
}
