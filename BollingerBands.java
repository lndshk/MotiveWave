package com.motivewave.platform.study.overlay;

import java.awt.Color;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.MarkerInfo;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.X11Colors;
import com.motivewave.platform.sdk.common.desc.DoubleDescriptor;
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
import com.motivewave.platform.sdk.study.RuntimeDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Bollinger Bands */
@StudyHeader(
    namespace="com.motivewave", 
    id="BOLLINGER_BANDS", 
    rb="com.motivewave.platform.study.nls.strings",
    name="TITLE_BB", 
    menu="MENU_OVERLAY",
    desc="DESC_BB",
    label="LBL_BB",
    overlay=true,
    studyOverlay=true,
    signals=true,
    helpLink="http://www.motivewave.com/studies/bollinger_bands.htm")
public class BollingerBands extends Study 
{
  final static String TOP_PERIOD = "topPeriod", TOP_SHIFT = "topShift", BOTTOM_PERIOD = "bottomPeriod", BOTTOM_SHIFT = "bottomShift";
  final static String TOP_STD = "topStd", BOTTOM_STD = "bottomStd";
  
  enum Values { TOP, MIDDLE, BOTTOM }
  enum Signals { CROSS_ABOVE_TOP_BAND, CROSS_BELOW_BOTTOM_BAND }

  @Override
  public void initialize(Defaults defaults)
  {
    SettingsDescriptor sd = new SettingsDescriptor();
    SettingTab tab = new SettingTab(get("TAB_GENERAL"));
    sd.addTab(tab);
    setSettingsDescriptor(sd);

    SettingGroup inputs = new SettingGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new MAMethodDescriptor(Inputs.METHOD, get("LBL_METHOD"), Enums.MAMethod.SMA));
    inputs.addRow(new IntegerDescriptor(TOP_PERIOD, get("LBL_TOP_PERIOD"), 20, 1, 9999, 1),
        new IntegerDescriptor(TOP_SHIFT, get("LBL_SHIFT"), 0, -999, 999, 1));
    inputs.addRow(new IntegerDescriptor(BOTTOM_PERIOD, get("LBL_BOTTOM_PERIOD"), 20, 1, 9999, 1),
        new IntegerDescriptor(BOTTOM_SHIFT, get("LBL_SHIFT"), 0, -999, 999, 1));
    inputs.addRow(new DoubleDescriptor(TOP_STD, get("LBL_TOP_STD"), 2.0, 0.1, 999, 0.1));
    inputs.addRow(new DoubleDescriptor(BOTTOM_STD, get("LBL_BOTTOM_STD"), 2.0, 0.1, 999, 0.1));
    tab.addGroup(inputs);
    
    SettingGroup colors = new SettingGroup(get("LBL_COLORS"));
    colors.addRow(new PathDescriptor(Inputs.PATH, get("LBL_BAND_LINES"), X11Colors.CADET_BLUE, 1.0f, null, true, true, false));
    colors.addRow(new PathDescriptor(Inputs.MIDDLE_PATH, get("LBL_MIDDLE_LINE"), X11Colors.DARK_SLATE_GRAY, 1.0f, new float[] {3f, 3f}, true, true, true));
    colors.addRow(new ShadeDescriptor(Inputs.FILL, get("LBL_FILL_COLOR"), Inputs.PATH, Inputs.PATH, Enums.ShadeType.BOTH, defaults.getFillColor(), false, true));
    tab.addGroup(colors);
    
    tab = new SettingTab(get("TAB_ADVANCED"));
    sd.addTab(tab);
    
    SettingGroup markers = new SettingGroup(get("LBL_MARKERS"));
    tab.addGroup(markers);
    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_CROSS_TOP_BAND"), 
        Enums.MarkerType.LINE_ARROW, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_CROSS_BOTTOM_BAND"), 
        Enums.MarkerType.LINE_ARROW, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), true, true));

    SettingGroup indicators = new SettingGroup(get("LBL_INDICATORS"));
    tab.addGroup(indicators);
    indicators.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_TOP_BOTTOM_IND"), X11Colors.CADET_BLUE, Color.WHITE, false, false, true));
    indicators.addRow(new IndicatorDescriptor(Inputs.MIDDLE_IND, get("LBL_MIDDLE_IND"), X11Colors.DARK_SLATE_GRAY, Color.WHITE, false, false, true));
    
    RuntimeDescriptor desc = new RuntimeDescriptor();
    desc.setLabelSettings(TOP_PERIOD, BOTTOM_PERIOD, TOP_STD, BOTTOM_STD);
    
    desc.exportValue(new ValueDescriptor(Values.TOP, get("LBL_BB_TOP"), new String[] {Inputs.INPUT, TOP_PERIOD, TOP_STD, TOP_SHIFT}));
    desc.exportValue(new ValueDescriptor(Values.MIDDLE, get("LBL_BB_MID"), new String[] {Inputs.INPUT, TOP_PERIOD, TOP_STD, TOP_SHIFT}));
    desc.exportValue(new ValueDescriptor(Values.BOTTOM, get("LBL_BB_BOTTOM"), new String[] {Inputs.INPUT, BOTTOM_PERIOD, BOTTOM_STD, BOTTOM_SHIFT}));
    
    desc.declarePath(Values.TOP, Inputs.PATH);
    desc.declarePath(Values.MIDDLE, Inputs.MIDDLE_PATH);
    desc.declarePath(Values.BOTTOM, Inputs.PATH);
    
    desc.declareIndicator(Values.TOP, Inputs.IND);
    desc.declareIndicator(Values.MIDDLE, Inputs.MIDDLE_IND);
    desc.declareIndicator(Values.BOTTOM, Inputs.IND);
    
    desc.setRangeKeys(Values.TOP, Values.BOTTOM);
    // Signals
    desc.declareSignal(Signals.CROSS_ABOVE_TOP_BAND, get("LBL_CROSS_TOP_BAND"));
    desc.declareSignal(Signals.CROSS_BELOW_BOTTOM_BAND, get("LBL_CROSS_BOTTOM_BAND"));
    
    setRuntimeDescriptor(desc);
  }

  @Override
  public int getMinBars()
  {
    int shift = Util.maxInt(getSettings().getInteger(TOP_SHIFT), getSettings().getInteger(BOTTOM_SHIFT));
    return Util.maxInt(getSettings().getInteger(TOP_PERIOD), getSettings().getInteger(BOTTOM_PERIOD)) + (shift > 0 ? shift : 0);
  }

  @Override
  public void onBarUpdate(DataContext ctx)
  {
    if (!getSettings().isBarUpdates()) return;
    doUpdate(ctx);
  }

  @Override
  public void onBarClose(DataContext ctx)
  {
    doUpdate(ctx);
  }
  
  private void doUpdate(DataContext ctx)
  {
    int topPeriod = getSettings().getInteger(TOP_PERIOD);
    int bottomPeriod = getSettings().getInteger(BOTTOM_PERIOD);
    int topShift = getSettings().getInteger(TOP_SHIFT);
    int bottomShift = getSettings().getInteger(BOTTOM_SHIFT);
    double topStd = getSettings().getDouble(TOP_STD);
    double bottomStd = getSettings().getDouble(BOTTOM_STD);
    Object input = getSettings().getInput(Inputs.INPUT);
    DataSeries series = ctx.getDataSeries();
    int latest = series.size()-1;
    Enums.MAMethod method = getSettings().getMAMethod(Inputs.METHOD, Enums.MAMethod.SMA);
    
    Double topStdDev = series.std(latest, topPeriod, input);
    Double bottomStdDev = series.std(latest, bottomPeriod, input);
    Double tma = series.ma(method, latest, topPeriod, input);
    Double bma = series.ma(method, latest, bottomPeriod, input);

    if (topStdDev == null || bottomStdDev == null || tma == null || bma == null) return;

    series.setDouble(latest+topShift, Values.MIDDLE, tma);
    series.setDouble(latest+topShift, Values.TOP, tma + topStdDev*topStd);
    series.setDouble(latest+bottomShift, Values.BOTTOM, bma - bottomStdDev*bottomStd);
    
    checkTopBand(ctx, latest);
    checkBottomBand(ctx, latest);
  }
  
  private void checkTopBand(DataContext ctx, int i)  
  {
    DataSeries series = ctx.getDataSeries();
    Double top = series.getDouble(i, Values.TOP);
    if (top == null) return;
    Coordinate c = new Coordinate(series.getStartTime(i), top);
    if (crossedAbove(series, i, Enums.BarInput.CLOSE, Values.TOP) && !series.getBoolean(i, Signals.CROSS_ABOVE_TOP_BAND, false)) {
      series.setBoolean(i, Signals.CROSS_ABOVE_TOP_BAND, true);
      MarkerInfo marker = getSettings().getMarker(Inputs.UP_MARKER);
      String msg = get("CROSS_ABOVE_TOP_BAND", format(series.getClose(i)), format(top));
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker, msg));
      ctx.signal(i, Signals.CROSS_ABOVE_TOP_BAND, msg, round(series.getClose(i)));
    }
  }

  private void checkBottomBand(DataContext ctx, int i)  
  {
    DataSeries series = ctx.getDataSeries();
    Double bottom = series.getDouble(i, Values.BOTTOM);
    if (bottom == null) return;
    Coordinate c = new Coordinate(series.getStartTime(i), bottom);
    if (crossedBelow(series, i, Enums.BarInput.CLOSE, Values.BOTTOM) && !series.getBoolean(i, Signals.CROSS_BELOW_BOTTOM_BAND, false)) {
      series.setBoolean(i, Signals.CROSS_BELOW_BOTTOM_BAND, true);
      MarkerInfo marker = getSettings().getMarker(Inputs.DOWN_MARKER);
      String msg = get("CROSS_BELOW_BOTTOM_BAND", format(series.getClose(i)), format(bottom));
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker, msg));
      ctx.signal(i, Signals.CROSS_BELOW_BOTTOM_BAND, msg, round(series.getClose(i)));
    }
  }

  @Override
  protected synchronized void calculateValues(DataContext ctx)
  {
    int topPeriod = getSettings().getInteger(TOP_PERIOD);
    int bottomPeriod = getSettings().getInteger(BOTTOM_PERIOD);
    int topShift = getSettings().getInteger(TOP_SHIFT);
    int bottomShift = getSettings().getInteger(BOTTOM_SHIFT);
    double topStd = getSettings().getDouble(TOP_STD);
    double bottomStd = getSettings().getDouble(BOTTOM_STD);
    Object input = getSettings().getInput(Inputs.INPUT);
    DataSeries series = ctx.getDataSeries();
    int latest = series.size()-1;
    Enums.MAMethod method = getSettings().getMAMethod(Inputs.METHOD, Enums.MAMethod.SMA);

    int end = latest;
    if (topShift < 0) end -= topShift; // calculate future values
    
    // Calculate top and middle lines
    boolean updates = getSettings().isBarUpdates();
    for(int i = topPeriod; i <= end; i++) {
      if (series.isComplete(i, Values.TOP)) continue;
      if (!updates && !series.isBarComplete(i)) continue;
      Double stdDev = series.std(i, topPeriod, input);
      Double ma = series.ma(method, i, topPeriod, input);
      if (stdDev == null || ma == null) {
        continue;
      }
      series.setDouble(i+topShift, Values.MIDDLE, ma);
      series.setDouble(i+topShift, Values.TOP, ma + stdDev*topStd);
      series.setComplete(i, Values.TOP, i >= 0 && i < latest); // latest bar is not complete
      checkTopBand(ctx, i);
    }    
    
    // Calculate bottom line
    end = latest;
    if (bottomShift < 0) end -= bottomShift; // calculate future values

    for(int i = bottomPeriod; i <= end; i++) {
      if (series.isComplete(i, Values.BOTTOM)) continue;
      if (!updates && !series.isBarComplete(i)) continue;
      Double stdDev = series.std(i, bottomPeriod, input);
      Double ma = series.ma(method, i, bottomPeriod, input);
      if (stdDev == null || ma == null) continue;
      series.setDouble(i+bottomShift, Values.BOTTOM, ma - stdDev*bottomStd);
      series.setComplete(i, Values.BOTTOM, i >= 0 && i < latest); // latest bar is not complete
      checkBottomBand(ctx, i);
    } 
  }
}
