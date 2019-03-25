package study_examples;

import com.motivewave.platform.sdk.common.BarSize;
import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Enums.MAMethod;
import com.motivewave.platform.sdk.common.GuideInfo;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.LineInfo;
import com.motivewave.platform.sdk.common.MarkerInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BarDescriptor;
import com.motivewave.platform.sdk.common.desc.BarSizeDescriptor;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.GuideDescriptor;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingGroup;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.SettingsDescriptor;
import com.motivewave.platform.sdk.common.desc.ShadeDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.Plot;
import com.motivewave.platform.sdk.study.RuntimeDescriptor;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Combines a MACD, Moving Average and RSI into one study. */
@StudyHeader(
    namespace="com.motivewave", 
    id="COMP_SAMPLE", 
    rb="study_examples.nls.strings", // locale specific strings are loaded from here
    name="TITLE_COMP_SAMPLE",
    desc="DESC_COMP_SAMPLE",
    overlay=false,
    signals=true)
public class CompositeSample extends com.motivewave.platform.sdk.study.Study 
{
	enum Values { MA, MACD, SIGNAL, HIST, RSI, UP, DOWN };
  enum Signals { CROSS_ABOVE, CROSS_BELOW, RSI_TOP, RSI_BOTTOM };

  final static String MA_PERIOD = "maPeriod";
  final static String MA_INPUT = "maInput";
  final static String MA_METHOD = "maMethod";
  final static String MACD_INPUT = "macdInput";
  final static String MACD_METHOD = "macdMethod";
  final static String MACD_PERIOD1 = "macdPeriod1";
  final static String MACD_PERIOD2 = "macdPeriod2";
  final static String RSI_PERIOD = "rsiPeriod";
  final static String RSI_INPUT = "rsiInput";
  final static String RSI_METHOD = "rsiMethod";

	final static String MACD_LINE = "macdLine";
  final static String MACD_IND = "macdInd";
  final static String HIST_IND = "histInd";

  final static String RSI_LINE = "rsiLine";
  final static String RSI_IND = "rsiInd";

  final static String RSI_PLOT = "RSIPlot";

  @Override
  public void initialize(Defaults defaults)
  {
    SettingsDescriptor sd = new SettingsDescriptor();
    setSettingsDescriptor(sd);
    
    SettingTab tab = new SettingTab(get("TAB_MA"));
    sd.addTab(tab);

    SettingGroup inputs = new SettingGroup(get("LBL_INPUTS"));
    inputs.addRow(new BarSizeDescriptor(Inputs.BARSIZE, get("LBL_BAR_SIZE"), BarSize.getBarSize(5)));
    inputs.addRow(new InputDescriptor(MA_INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(MA_PERIOD, get("LBL_PERIOD"), 20, 1, 9999, 1),
                  new IntegerDescriptor(Inputs.SHIFT, get("LBL_SHIFT"), 0, -999, 999, 1));
    inputs.addRow(new BooleanDescriptor(Inputs.FILL_FORWARD, get("LBL_FILL_FORWARD"), true));
    tab.addGroup(inputs);
    
    SettingGroup colors = new SettingGroup(get("LBL_DISPLAY"));
    colors.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), null, 1.0f, null, true, true, false));
    colors.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));
    tab.addGroup(colors);

    SettingGroup barColors = new SettingGroup(get("LBL_BAR_COLOR"));
    barColors.addRow(new BooleanDescriptor(Inputs.ENABLE_BAR_COLOR, get("LBL_ENABLE_BAR_COLOR"), false));
    barColors.addRow(new ColorDescriptor(Inputs.UP_COLOR, get("LBL_UP_COLOR"), defaults.getGreenLine()));
    barColors.addRow(new ColorDescriptor(Inputs.NEUTRAL_COLOR, get("LBL_NEUTRAL_COLOR"), defaults.getLineColor()));
    barColors.addRow(new ColorDescriptor(Inputs.DOWN_COLOR, get("LBL_DOWN_COLOR"), defaults.getRedLine()));
    tab.addGroup(barColors);

    sd.addDependency(new EnabledDependency(Inputs.ENABLE_BAR_COLOR, Inputs.UP_COLOR, Inputs.NEUTRAL_COLOR, Inputs.DOWN_COLOR ));

    tab = new SettingTab(get("TAB_MACD"));
    sd.addTab(tab);
    
    inputs = new SettingGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(MACD_INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new MAMethodDescriptor(MACD_METHOD, get("LBL_METHOD"), Enums.MAMethod.EMA));
    inputs.addRow(new MAMethodDescriptor(Inputs.SIGNAL_METHOD, get("LBL_SIGNAL_METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new IntegerDescriptor(MACD_PERIOD1, get("LBL_PERIOD1"), 12, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(MACD_PERIOD2, get("LBL_PERIOD2"), 26, 1, 9999, 1));
    inputs.addRow(new IntegerDescriptor(Inputs.SIGNAL_PERIOD, get("LBL_SIGNAL_PERIOD"), 9, 1, 9999, 1));
    tab.addGroup(inputs);
    
    SettingGroup lines = new SettingGroup(get("LBL_DISPLAY"));
    lines.addRow(new PathDescriptor(MACD_LINE, get("LBL_MACD_LINE"), defaults.getLineColor(), 1.5f, null, true, false, true));
    lines.addRow(new PathDescriptor(Inputs.SIGNAL_PATH, get("LBL_SIGNAL_LINE"), defaults.getRed(), 1.0f, null, true, false, true));
    lines.addRow(new BarDescriptor(Inputs.BAR, get("LBL_BAR_COLOR"), defaults.getBarColor(), true, true));
    lines.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getGreen(), defaults.getLineColor(), false, true));
    lines.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.VERY_SMALL, defaults.getRed(), defaults.getLineColor(), false, true));
    tab.addGroup(lines);

    SettingGroup indicators = new SettingGroup(get("LBL_INDICATORS"));
    indicators.addRow(new IndicatorDescriptor(MACD_IND, get("LBL_MACD_IND"), null, null, false, true, true));
    indicators.addRow(new IndicatorDescriptor(Inputs.SIGNAL_IND, get("LBL_SIGNAL_IND"), defaults.getRed(), null, false, false, true));
    indicators.addRow(new IndicatorDescriptor(HIST_IND, get("LBL_MACD_HIST_IND"), defaults.getBarColor(), null, false, false, true));
    tab.addGroup(indicators);

    tab = new SettingTab(get("TAB_RSI"));
    sd.addTab(tab);

    inputs = new SettingGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(RSI_INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new MAMethodDescriptor(RSI_METHOD, get("LBL_METHOD"), Enums.MAMethod.SMMA));
    inputs.addRow(new IntegerDescriptor(RSI_PERIOD, get("LBL_PERIOD"), 14, 1, 9999, 1));
    tab.addGroup(inputs);
    
    lines = new SettingGroup(get("LBL_LINES"));
    lines.addRow(new PathDescriptor(RSI_LINE, get("LBL_RSI_LINE"), defaults.getLineColor(), 1.0f, null));
    lines.addRow(new ShadeDescriptor(Inputs.TOP_FILL, get("LBL_TOP_FILL"), Inputs.TOP_GUIDE, RSI_LINE, Enums.ShadeType.ABOVE, defaults.getTopFillColor(), true, true));
    lines.addRow(new ShadeDescriptor(Inputs.BOTTOM_FILL, get("LBL_BOTTOM_FILL"), Inputs.BOTTOM_GUIDE, RSI_LINE, Enums.ShadeType.BELOW, defaults.getBottomFillColor(), true, true));
    lines.addRow(new IndicatorDescriptor(RSI_IND, get("LBL_RSI_IND"), null, null, false, true, true));
    tab.addGroup(lines);

    SettingGroup guides = new SettingGroup(get("LBL_GUIDES"));
    guides.addRow(new GuideDescriptor(Inputs.TOP_GUIDE, get("LBL_TOP_GUIDE"), 70, 1, 100, 1, true));
    GuideDescriptor mg = new GuideDescriptor(Inputs.MIDDLE_GUIDE, get("LBL_MIDDLE_GUIDE"), 50, 1, 100, 1, true);
    mg.setDash(new float[] {3, 3});
    guides.addRow(mg);
    guides.addRow(new GuideDescriptor(Inputs.BOTTOM_GUIDE, get("LBL_BOTTOM_GUIDE"), 30, 1, 100, 1, true));
    tab.addGroup(guides);
    
    RuntimeDescriptor desc = new RuntimeDescriptor();
    setRuntimeDescriptor(desc);

    desc.exportValue(new ValueDescriptor(Values.MA, "MA", new String[] {MA_INPUT, MA_PERIOD, Inputs.SHIFT, Inputs.BARSIZE}));
    desc.exportValue(new ValueDescriptor(Values.RSI, get("LBL_RSI"), new String[] {RSI_INPUT, RSI_PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.MACD, get("LBL_MACD"), new String[] {MACD_INPUT, MACD_METHOD, MACD_PERIOD1, MACD_PERIOD2}));
    desc.exportValue(new ValueDescriptor(Values.SIGNAL, get("LBL_MACD_SIGNAL"), new String[] {Inputs.SIGNAL_PERIOD}));
    desc.exportValue(new ValueDescriptor(Values.HIST, get("LBL_MACD_HIST"), new String[] {MACD_PERIOD1, MACD_PERIOD2, Inputs.SIGNAL_PERIOD}));

    desc.declareSignal(Signals.CROSS_ABOVE, get("LBL_CROSS_ABOVE_SIGNAL"));
    desc.declareSignal(Signals.CROSS_BELOW, get("LBL_CROSS_BELOW_SIGNAL"));
    desc.declareSignal(Signals.RSI_TOP, get("RSI_TOP"));
    desc.declareSignal(Signals.RSI_BOTTOM, get("RSI_BOTTOM"));
    
    // Price plot (moving average)
    desc.getPricePlot().setLabelSettings(MA_INPUT, MA_PERIOD, Inputs.SHIFT, Inputs.BARSIZE);
    desc.getPricePlot().setLabelPrefix("MA");
    desc.getPricePlot().declarePath(Values.MA, Inputs.PATH);
    desc.getPricePlot().declareIndicator(Values.MA, Inputs.IND);
    // This tells MotiveWave that the MA values come from the data series defined by "BARSIZE"
    desc.setValueSeries(Values.MA, Inputs.BARSIZE);

    // Default Plot (MACD)
    desc.setLabelSettings(MACD_INPUT, MACD_METHOD, MACD_PERIOD1, MACD_PERIOD2, Inputs.SIGNAL_PERIOD);
    desc.setLabelPrefix("MACD");
    desc.setTabName("MACD");
    desc.declarePath(Values.MACD, MACD_LINE);
    desc.declarePath(Values.SIGNAL, Inputs.SIGNAL_PATH);
    desc.declareBars(Values.HIST, Inputs.BAR);
    desc.declareIndicator(Values.MACD, MACD_IND);
    desc.declareIndicator(Values.SIGNAL, Inputs.SIGNAL_IND);
    desc.declareIndicator(Values.HIST, HIST_IND);
    desc.setRangeKeys(Values.MACD, Values.SIGNAL, Values.HIST);
    desc.addHorizontalLine(new LineInfo(0, null, 1.0f, new float[] {3,3}));
    
    // RSI Plot
    Plot rsiPlot = new Plot();
    desc.addPlot(RSI_PLOT, rsiPlot);
    
    rsiPlot.setLabelSettings(RSI_INPUT, RSI_PERIOD);
    rsiPlot.setLabelPrefix("RSI");
    rsiPlot.setTabName("RSI");
    rsiPlot.declarePath(Values.RSI, RSI_LINE);
    rsiPlot.declareIndicator(Values.RSI, RSI_IND);
    rsiPlot.declareGuide(Inputs.TOP_GUIDE);
    rsiPlot.declareGuide(Inputs.MIDDLE_GUIDE);
    rsiPlot.declareGuide(Inputs.BOTTOM_GUIDE);
    rsiPlot.setMaxBottomValue(15);
    rsiPlot.setMinTopValue(85);
    rsiPlot.setRangeKeys(Values.RSI);
    rsiPlot.setMinTick(0.1);
  }

  // Since the Moving Average (MA) is plotted on a different data series, we need to override this method 
  // and manually compute the MA for the secondary data series.
  @Override
  protected void calculateValues(DataContext ctx)
  {
    int maPeriod = getSettings().getInteger(MA_PERIOD);
    Object maInput = getSettings().getInput(MA_INPUT, Enums.BarInput.CLOSE);
    BarSize barSize = getSettings().getBarSize(Inputs.BARSIZE);
    DataSeries series2 = ctx.getDataSeries(barSize);

    StudyHeader header = getHeader();
    boolean updates = getSettings().isBarUpdates() || (header != null && header.requiresBarUpdates());

    // Calculate Moving Average for the Secondary Data Series
    for(int i = 1; i < series2.size(); i++) {
      if (series2.isComplete(i)) continue;
      if (!updates && !series2.isBarComplete(i)) continue;
      Double sma = series2.ma(MAMethod.SMA, i, maPeriod, maInput);
      series2.setDouble(i, Values.MA, sma);
    }

    // Invoke the parent method to run the "calculate" method below for the primary (chart) data series
    super.calculateValues(ctx);
  }
  
  // Computes the values for the MACD and RSI plots.  These plots use the primary (chart) data series.
  @Override  
  protected void calculate(int index, DataContext ctx)
  {
    DataSeries series = ctx.getDataSeries();
    boolean complete=true;
    
    int macdPeriod1 = getSettings().getInteger(MACD_PERIOD1, 12);
    int macdPeriod2 = getSettings().getInteger(MACD_PERIOD2, 26);
    Enums.MAMethod macdMethod = getSettings().getMAMethod(MACD_METHOD, Enums.MAMethod.EMA);
    Object macdInput = getSettings().getInput(MACD_INPUT, Enums.BarInput.CLOSE);
    if (index >= Util.max(macdPeriod1, macdPeriod2)) {
      Double MA1 = null, MA2 = null;
      MA1 = series.ma(macdMethod, index, macdPeriod1, macdInput);
      MA2 = series.ma(macdMethod, index, macdPeriod2, macdInput);
      
      double MACD = MA1 - MA2; 
      series.setDouble(index, Values.MACD, MACD);

      int signalPeriod = getSettings().getInteger(Inputs.SIGNAL_PERIOD, 9);

      // Calculate moving average of MACD (signal line)
      Double signal = series.ma(getSettings().getMAMethod(Inputs.SIGNAL_METHOD, Enums.MAMethod.SMA), index, signalPeriod, Values.MACD);
      series.setDouble(index, Values.SIGNAL, signal);
    
      if (signal != null) series.setDouble(index, Values.HIST, MACD - signal);

      if (series.isBarComplete(index)) {
        // Check for signal events
        Coordinate c = new Coordinate(series.getStartTime(index), signal);
        if (crossedAbove(series, index, Values.MACD, Values.SIGNAL)) {
          MarkerInfo marker = getSettings().getMarker(Inputs.UP_MARKER);
          String msg = get("SIGNAL_MACD_CROSS_ABOVE", MACD, signal);
          if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
          ctx.signal(index, Signals.CROSS_ABOVE, msg, signal);
        }
        else if (crossedBelow(series, index, Values.MACD, Values.SIGNAL)) {
          MarkerInfo marker = getSettings().getMarker(Inputs.DOWN_MARKER);
          String msg = get("SIGNAL_MACD_CROSS_BELOW", MACD, signal);
          if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
          ctx.signal(index, Signals.CROSS_BELOW, msg, signal);
        }
      }
    }
    else complete=false;
    
    int rsiPeriod = getSettings().getInteger(RSI_PERIOD);
    Object rsiInput = getSettings().getInput(RSI_INPUT);
    if (index < 1) return; // not enough data
    
    double diff = series.getDouble(index, rsiInput) - series.getDouble(index-1, rsiInput);
    double up = 0, down = 0;
    if (diff > 0) up = diff;
    else down = diff;
    
    series.setDouble(index, Values.UP, up);
    series.setDouble(index, Values.DOWN, Math.abs(down));
    
    if (index <= rsiPeriod +1) return;
    
    Enums.MAMethod method = getSettings().getMAMethod(RSI_METHOD);
    double avgUp = series.ma(method, index, rsiPeriod, Values.UP);
    double avgDown = series.ma(method, index, rsiPeriod, Values.DOWN);
    double RS = avgUp / avgDown;
    double RSI = 100.0 - ( 100.0 / (1.0 + RS));

    series.setDouble(index, Values.RSI, RSI);
    
    // Do we need to generate a signal?
    GuideInfo topGuide = getSettings().getGuide(Inputs.TOP_GUIDE);
    GuideInfo bottomGuide = getSettings().getGuide(Inputs.BOTTOM_GUIDE);
    if (crossedAbove(series, index, Values.RSI, topGuide.getValue())) {
      series.setBoolean(index, Signals.RSI_TOP, true);
      ctx.signal(index, Signals.RSI_TOP, get("SIGNAL_RSI_TOP", topGuide.getValue(), round(RSI)), round(RSI));
    }
    else if (crossedBelow(series, index, Values.RSI, bottomGuide.getValue())) {
      series.setBoolean(index, Signals.RSI_BOTTOM, true);
      ctx.signal(index, Signals.RSI_BOTTOM, get("SIGNAL_RSI_BOTTOM", bottomGuide.getValue(), round(RSI)), round(RSI));
    }
    
    series.setComplete(index, complete);
  }  
}
