package study_examples;

import com.motivewave.platform.sdk.common.Coordinate;
import com.motivewave.platform.sdk.common.DataContext;
import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Defaults;
import com.motivewave.platform.sdk.common.Enums;
import com.motivewave.platform.sdk.common.Inputs;
import com.motivewave.platform.sdk.common.MarkerInfo;
import com.motivewave.platform.sdk.common.desc.InputDescriptor;
import com.motivewave.platform.sdk.common.desc.IntegerDescriptor;
import com.motivewave.platform.sdk.common.desc.MAMethodDescriptor;
import com.motivewave.platform.sdk.common.desc.MarkerDescriptor;
import com.motivewave.platform.sdk.common.desc.PathDescriptor;
import com.motivewave.platform.sdk.common.desc.SettingGroup;
import com.motivewave.platform.sdk.common.desc.SettingTab;
import com.motivewave.platform.sdk.common.desc.SettingsDescriptor;
import com.motivewave.platform.sdk.common.desc.ValueDescriptor;
import com.motivewave.platform.sdk.draw.Marker;
import com.motivewave.platform.sdk.study.RuntimeDescriptor;
import com.motivewave.platform.sdk.study.Study;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Moving Average Cross. This study consists of two moving averages: 
    Fast MA (shorter period), Slow MA. Signals are generated when the 
    Fast MA moves above or below the Slow MA. Markers are also displayed 
    where these crosses occur. */
@StudyHeader(
  namespace="com.mycompany", 
  id="MACROSS", 
  name="Sample Moving Average Cross",
  label="MA Cross",
  desc="Displays a signal arrow when two moving averages (fast and slow) cross.",
  menu="Examples",
  overlay=true,
  signals=true)
public class SampleMACross extends Study
{
  enum Values { FAST_MA, SLOW_MA };
  enum Signals { CROSS_ABOVE, CROSS_BELOW };

  @Override
  public void initialize(Defaults defaults)
  {
    // User Settings
    SettingsDescriptor sd=new SettingsDescriptor();
    setSettingsDescriptor(sd);
    SettingTab tab=new SettingTab("General");
    sd.addTab(tab);

    // Fast MA (shorter period)
    SettingGroup ma1=new SettingGroup("Fast MA");
    tab.addGroup(ma1);
    ma1.addRow(new InputDescriptor(Inputs.INPUT, "Fast Input", Enums.BarInput.CLOSE));
    ma1.addRow(new MAMethodDescriptor(Inputs.METHOD, "Fast Method", Enums.MAMethod.EMA));
    ma1.addRow(new IntegerDescriptor(Inputs.PERIOD, "Fast Period", 10, 1, 9999, 1));

    // Slow MA (shorter period)
    SettingGroup ma2=new SettingGroup("Slow MA");
    tab.addGroup(ma2);
    ma2.addRow(new InputDescriptor(Inputs.INPUT2, "Slow Input", Enums.BarInput.CLOSE));
    ma2.addRow(new MAMethodDescriptor(Inputs.METHOD2, "Slow Method", Enums.MAMethod.EMA));
    ma2.addRow(new IntegerDescriptor(Inputs.PERIOD2, "Slow Period", 20, 1, 9999, 1));

    tab=new SettingTab("Display");
    sd.addTab(tab);

    SettingGroup lines=new SettingGroup("Lines");
    tab.addGroup(lines);
    lines.addRow(new PathDescriptor(Inputs.PATH, "Fast MA", defaults.getGreenLine(), 1.0f, null, true, false, false));
    lines.addRow(new PathDescriptor(Inputs.PATH2, "Slow MA", defaults.getBlueLine(), 1.0f, null, true, false, false));

    SettingGroup markers=new SettingGroup("Markers");
    tab.addGroup(markers);
    markers.addRow(new MarkerDescriptor(Inputs.UP_MARKER, "Up Marker", Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getGreen(), defaults.getLineColor(), true, true));
    markers.addRow(new MarkerDescriptor(Inputs.DOWN_MARKER, "Down Marker", Enums.MarkerType.TRIANGLE, Enums.Size.SMALL, defaults.getRed(), defaults.getLineColor(), true, true));

    // Runtime Settings
    RuntimeDescriptor desc=new RuntimeDescriptor();
    setRuntimeDescriptor(desc);

    desc.setLabelSettings(Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD, Inputs.INPUT2, Inputs.METHOD2, Inputs.PERIOD2);
    desc.exportValue(new ValueDescriptor(Values.FAST_MA, "Fast MA", new String[] { Inputs.INPUT, Inputs.METHOD, Inputs.PERIOD }));
    desc.exportValue(new ValueDescriptor(Values.SLOW_MA, "Slow MA", new String[] { Inputs.INPUT2, Inputs.METHOD2, Inputs.PERIOD2 }));
    desc.exportValue(new ValueDescriptor(Signals.CROSS_ABOVE, Enums.ValueType.BOOLEAN, "Cross Above", null));
    desc.exportValue(new ValueDescriptor(Signals.CROSS_BELOW, Enums.ValueType.BOOLEAN, "Cross Below", null));
    desc.declarePath(Values.FAST_MA, Inputs.PATH);
    desc.declarePath(Values.SLOW_MA, Inputs.PATH2);

    // Signals
    desc.declareSignal(Signals.CROSS_ABOVE, "Fast MA Cross Above");
    desc.declareSignal(Signals.CROSS_BELOW, "Fast MA Cross Below");

    desc.setRangeKeys(Values.FAST_MA, Values.SLOW_MA);
  }

  @Override
  protected void calculate(int index, DataContext ctx)
  {
    int fastPeriod=getSettings().getInteger(Inputs.PERIOD);
    int slowPeriod=getSettings().getInteger(Inputs.PERIOD2);
    if (index < Math.max(fastPeriod, slowPeriod)) return; // not enough data

    DataSeries series=ctx.getDataSeries();

    // Calculate and store the fast and slow MAs
    Double fastMA=series.ma(getSettings().getMAMethod(Inputs.METHOD), index, fastPeriod, getSettings().getInput(Inputs.INPUT));
    Double slowMA=series.ma(getSettings().getMAMethod(Inputs.METHOD2), index, slowPeriod, getSettings().getInput(Inputs.INPUT2));
    if (fastMA == null || slowMA == null) return;

    series.setDouble(index, Values.FAST_MA, fastMA);
    series.setDouble(index, Values.SLOW_MA, slowMA);

    if (!series.isBarComplete(index)) return;

    // Check to see if a cross occurred and raise signal.
    Coordinate c=new Coordinate(series.getStartTime(index), slowMA);
    if (crossedAbove(series, index, Values.FAST_MA, Values.SLOW_MA)) {
      MarkerInfo marker=getSettings().getMarker(Inputs.UP_MARKER);
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.BOTTOM, marker));
      ctx.signal(index, Signals.CROSS_ABOVE, "Fast MA Crossed Above!", series.getClose(index));
    }
    else if (crossedBelow(series, index, Values.FAST_MA, Values.SLOW_MA)) {
      MarkerInfo marker=getSettings().getMarker(Inputs.DOWN_MARKER);
      if (marker.isEnabled()) addFigure(new Marker(c, Enums.Position.TOP, marker));
      ctx.signal(index, Signals.CROSS_BELOW, "Fast MA Crossed Below!", series.getClose(index));
    }

    series.setComplete(index);
  }
}
