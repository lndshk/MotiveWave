package study_examples;

import com.motivewave.platform.sdk.common.DataSeries;
import com.motivewave.platform.sdk.common.Instrument;
import com.motivewave.platform.sdk.order_mgmt.OrderContext;
import com.motivewave.platform.sdk.study.StudyHeader;

/** Moving Average Cross Strategy. This is based of the SampleMACross study and adds the ability to trade. */
@StudyHeader(
  namespace="com.mycompany", 
  id="MACROSS_STRATEGY", 
  name="Sample MA Cross Strategy",
  desc="Buys when the fast MA crosses above the slow MA and sells when it crosses below.",
  menu="Examples",
  overlay = true,
  signals = true,
  strategy = true,
  autoEntry = true,
  manualEntry = false,
  supportsUnrealizedPL = true,
  supportsRealizedPL = true,
  supportsTotalPL = true)
public class SampleMACrossStrategy extends SampleMACross
{
  @Override
  public void onActivate(OrderContext ctx)
  {
    if (getSettings().isEnterOnActivate()) {
      DataSeries series = ctx.getDataContext().getDataSeries();
      int ind = series.isLastBarComplete() ? series.size()-1 : series.size()-2;
      Double fastMA = series.getDouble(ind, Values.FAST_MA);
      Double slowMA = series.getDouble(ind, Values.SLOW_MA);
      if (fastMA == null || slowMA == null) return;
      int tradeLots = getSettings().getTradeLots();
      int qty = tradeLots *= ctx.getInstrument().getDefaultQuantity();
      // Create a long or short position if we are above or below the signal line
      if (fastMA > slowMA) ctx.buy(qty);
      else ctx.sell(qty);
    }
  }

  @Override
  public void onSignal(OrderContext ctx, Object signal)
  {
    Instrument instr = ctx.getInstrument();
    int position = ctx.getPosition();
    int qty = (getSettings().getTradeLots() * instr.getDefaultQuantity());

    qty += Math.abs(position); // Stop and Reverse if there is an open position
    if (position <= 0 && signal == Signals.CROSS_ABOVE) {
      ctx.buy(qty); // Open Long Position
    }
    if (position >= 0 && signal == Signals.CROSS_BELOW) {
      ctx.sell(qty); // Open Short Position
    }
  }
}
