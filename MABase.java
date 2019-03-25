package com.motivewave.platform.study.ma;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.MarkerInfo;
import com.motivewave.platform.sdk.common.Settings;
import com.motivewave.platform.sdk.common.Util;
import com.motivewave.platform.sdk.common.desc.BooleanDescriptor;
import com.motivewave.platform.sdk.common.desc.ColorDescriptor;
import com.motivewave.platform.sdk.common.desc.EnabledDependency;
import com.motivewave.platform.sdk.common.desc.IndicatorDescriptor;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingGroup;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.SettingsDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.RuntimeDescriptor;
import com.motivewave.platform.sdk.study.Study;

/** Base Class for All Moving Averages. */
public class MABase extends Study
{
  enum Values { MA }
  enum Signals { CROSS_ABOVE, CROSS_BELOW }
  
  protected String MA_LABEL = "";
  protected Enums.MAMethod METHOD;

  @Override
  public void initialize(Defaults defaults)
  {
    SettingsDescriptor sd = new SettingsDescriptor();
    setSettingsDescriptor(sd);

    SettingTab tab = new SettingTab(get("TAB_GENERAL"));
    sd.addTab(tab);

    SettingGroup inputs = new SettingGroup(get("LBL_INPUTS"));
    inputs.addRow(new InputDescriptor(Inputs.INPUT, get("LBL_INPUT"), Enums.BarInput.CLOSE));
    inputs.addRow(new IntegerDescriptor(Inputs.PERIOD, get("LBL_PERIOD"), 20, 1, 9999, 1),
                  new IntegerDescriptor(Inputs.SHIFT, get("LBL_SHIFT"), 0, -999, 999, 1));
    inputs.addRow(new BooleanDescriptor(Inputs.FILL_FORWARD, get("LBL_FILL_FORWARD"), true));
    tab.addGroup(inputs);
    
    SettingGroup colors = new SettingGroup(get("LBL_DISPLAY"));
    colors.addRow(new PathDescriptor(Inputs.PATH, get("LBL_LINE"), null, 1.0f, null, true, true, false));
    colors.addRow(new IndicatorDescriptor(Inputs.IND, get("LBL_INDICATOR"), null, null, false, true, true));
    tab.addGroup(colors);

    tab = new SettingTab(get("TAB_ADVANCED"));
    sd.addTab(tab);

    SettingGroup barColors = new SettingGroup(get("LBL_BAR_COLOR"));
    barColors.addRow(new BooleanDescriptor(Inputs.ENABLE_BAR_COLOR, get("LBL_ENABLE_BAR_COLOR"), false));
    barColors.addRow(new ColorDescriptor(Inputs.UP_COLOR, get("LBL_UP_COLOR"), defaults.getGreenLine()));
    barColors.addRow(new ColorDescriptor(Inputs.NEUTRAL_COLOR, get("LBL_NEUTRAL_COLOR"), defaults.getLineColor()));
    barColors.addRow(new ColorDescriptor(Inputs.DOWN_COLOR, get("LBL_DOWN_COLOR"), defaults.getRedLine()));
    tab.addGroup(barColors);

    sd.addDependency(new EnabledDependency(Inputs.ENABLE_BAR_COLOR, Inputs.UP_COLOR, Inputs.NEUTRAL_COLOR, Inputs.DOWN_COLOR ));

    SettingGroup markers = new SettingGroup(get("LBL_MARKERS"));
    tab.addGroup(markers);
    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, get("LBL_UP_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), false, true));
    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, get("LBL_DOWN_MARKER"), 
        Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), false, true));
    
    RuntimeDescriptor desc = new RuntimeDescriptor();
    
    desc.setLabelSettings(Inputs.INPUT, Inputs.PERIOD, Inputs.SHIFT);
    desc.exportValue(new ValueDescriptor(Values.MA, MA_LABEL, new String[] {Inputs.INPUT, Inputs.PERIOD, Inputs.SHIFT}));
    desc.declarePath(Values.MA, Inputs.PATH);
    desc.declareIndicator(Values.MA, Inputs.IND);

    // Signals
    desc.declareSignal(Signals.CROSS_ABOVE, get("LBL_PRICE_CROSS_ABOVE"));
    desc.declareSignal(Signals.CROSS_BELOW, get("LBL_PRICE_CROSS_BELOW"));

    setRuntimeDescriptor(desc);
  }

  @Override
  public int getMinBars()
  {
    int shift = getSettings().getInteger(Inputs.SHIFT);
    int bars = getSettings().getInteger(Inputs.PERIOD) + (shift > 0 ? shift : 0);

    // For some moving averages, more data is needed to produce useful results.
    // For example, an EMA the first value is based on an SMA.  In these cases, double the number of required bars
    Enums.MAMethod method = METHOD;
    if (Util.in(method, Enums.MAMethod.EMA, Enums.MAMethod.SMMA, Enums.MAMethod.DEMA, Enums.MAMethod.KAMA, Enums.MAMethod.MEMA)) bars *= 2;
    return bars;
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

  protected void doUpdate(DataContext ctx)
  {
    Settings settings = getSettings();
    if (settings == null) return;
    Util.calcLatestMA(ctx, METHOD, settings.getInput(Inputs.INPUT), settings.getInteger(Inputs.PERIOD), 
        settings.getInteger(Inputs.SHIFT, 0), Values.MA, settings.getBoolean(Inputs.FILL_FORWARD, true));

    MarkerInfo upMarker = settings.getMarker(Inputs.UP_MARKER);
    MarkerInfo downMarker = settings.getMarker(Inputs.DOWN_MARKER);
    boolean doBarColor = settings.getBoolean(Inputs.ENABLE_BAR_COLOR, false);
    boolean upEnabled = upMarker != null && upMarker.isEnabled();
    boolean downEnabled = downMarker != null && downMarker.isEnabled();
    
    if (!doBarColor && !upEnabled && !downEnabled && !ctx.isSignalEnabled(Signals.CROSS_ABOVE) &&
        !ctx.isSignalEnabled(Signals.CROSS_BELOW)) return;

    DataSeries series = ctx.getDataSeries();
    Double val = series.getDouble(Values.MA);
    Double prevVal = series.getDouble(series.size()-2, Values.MA);
    if (val == null || prevVal == null) return;

    if (doBarColor) {
      if (val < prevVal) series.setPriceBarColor(settings.getColor(Inputs.DOWN_COLOR));
      else if (val == prevVal) series.setPriceBarColor(settings.getColor(Inputs.NEUTRAL_COLOR));
      else series.setPriceBarColor(settings.getColor(Inputs.UP_COLOR));
    }

    if (series.size() <= 1) return;
    
    double v = val;
    double pv = prevVal;
    float ma = series.getInstrument().round((float)v);
    float pma = series.getInstrument().round((float)pv);
    float close = series.getClose();
    float lastClose = series.getClose(series.size()-2);
    int i = series.size()-1;
    
    Coordinate c = new Coordinate(series.getStartTime(i), ma);
    if (upEnabled || ctx.isSignalEnabled(Signals.CROSS_ABOVE)) {
      if (lastClose <= pma && close > ma) {
        //series.setBoolean(i, Signals.CROSS_ABOVE, true);
        String msg = get("SIGNAL_PRICE_CROSS_ABOVE", format(close), format(ma));
        if (upEnabled && !series.getBoolean("UP_MARKER_ADDED", false)) {
          addFigure(new Marker(c, Enums.Position.BOTTOM, upMarker, msg));
          series.setBoolean("UP_MARKER_ADDED", true);
        }
        ctx.signal(i, Signals.CROSS_ABOVE, msg, round(close));
      }
    }

    if (downEnabled || ctx.isSignalEnabled(Signals.CROSS_BELOW)) {
      //series.setBoolean(i, Signals.CROSS_BELOW, true);
      if (lastClose >= pma && close < ma) {
        String msg = get("SIGNAL_PRICE_CROSS_BELOW", format(close), format(ma));
        if (downEnabled && !series.getBoolean("DOWN_MARKER_ADDED", false)) {
          addFigure(new Marker(c, Enums.Position.TOP, downMarker, msg));
          series.setBoolean("DOWN_MARKER_ADDED", true);
        }
        ctx.signal(i, Signals.CROSS_BELOW, msg, round(close));
      }
    }
  }

  @Override
  protected void calculateValues(DataContext ctx)
  {
    Settings settings = getSettings();
    if (settings == null) return;
    Util.calcSeriesMA(ctx, METHOD, settings.getInput(Inputs.INPUT), settings.getInteger(Inputs.PERIOD), 
        settings.getInteger(Inputs.SHIFT, 0), Values.MA, settings.getBoolean(Inputs.FILL_FORWARD, true), settings.isBarUpdates());
    MarkerInfo upMarker = settings.getMarker(Inputs.UP_MARKER);
    MarkerInfo downMarker = settings.getMarker(Inputs.DOWN_MARKER);
    boolean doBarColor = settings.getBoolean(Inputs.ENABLE_BAR_COLOR, false);
    boolean upEnabled = upMarker != null && upMarker.isEnabled();
    boolean downEnabled = downMarker != null && downMarker.isEnabled();
    
    if (!doBarColor && !upEnabled && !downEnabled && !ctx.isSignalEnabled(Signals.CROSS_ABOVE) &&
        !ctx.isSignalEnabled(Signals.CROSS_BELOW)) return;
    
    DataSeries series = ctx.getDataSeries();
    if (series == null) return;
    clearFigures();
    boolean updates = settings.isBarUpdates();
    for(int i = 1; i < series.size(); i++) {
      if (!updates && !series.isBarComplete(i)) continue;
      Double ma = series.getDouble(i, Values.MA);
      if (ma == null) continue;
      if (doBarColor) {
        Double prevMa = series.getDouble(i-1, Values.MA);
        if (prevMa == null) continue;
        if (ma < prevMa) series.setPriceBarColor(i, settings.getColor(Inputs.DOWN_COLOR));
        else if (ma == prevMa) series.setPriceBarColor(i,settings.getColor(Inputs.NEUTRAL_COLOR));
        else series.setPriceBarColor(i, settings.getColor(Inputs.UP_COLOR));
      }
      
      // Check to see if a cross occurred and raise signal.
      Coordinate c = new Coordinate(series.getStartTime(i), ma);
      float close = series.getClose(i);
      if (crossedAbove(series, i, Enums.BarInput.CLOSE, Values.MA)) {
        series.setBoolean(i, Signals.CROSS_ABOVE, true);
        String msg = get("SIGNAL_PRICE_CROSS_ABOVE", format(close), format(ma));
        if (upEnabled)addFigure(new Marker(c, Enums.Position.BOTTOM, upMarker, msg));
        ctx.signal(i, Signals.CROSS_ABOVE, msg, round(close));
      }
      else if (crossedBelow(series, i, Enums.BarInput.CLOSE, Values.MA)) {
        series.setBoolean(i, Signals.CROSS_BELOW, true);
        String msg = get("SIGNAL_PRICE_CROSS_BELOW", format(close), format(ma));
        if (downEnabled) addFigure(new Marker(c, Enums.Position.TOP, downMarker, msg));
        ctx.signal(i, Signals.CROSS_BELOW, msg, round(close));
      }
    }
  }
}
