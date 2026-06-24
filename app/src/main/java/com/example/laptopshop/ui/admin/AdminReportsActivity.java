package com.example.laptopshop.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.laptopshop.R;
import com.example.laptopshop.data.dao.DonHangDao;
import com.example.laptopshop.data.dao.NguoiDungDao;
import com.example.laptopshop.data.db.DBHelper;
import com.example.laptopshop.ui.auth.WelcomeActivity;
import com.example.laptopshop.ui.home.BaseHomeActivity;
import com.example.laptopshop.ui.orders.OrdersAdapter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AdminReportsActivity extends BaseHomeActivity {

    private DonHangDao donHangDao;
    private NguoiDungDao nguoiDungDao;

    @Override
    protected int shellLayoutRes() {
        return R.layout.activity_home_bottom_admin;
    }

    @Override
    protected int contentLayoutRes() {
        return R.layout.content_admin_reports;
    }

    @Override
    protected int bottomMenuRes() {
        return R.menu.menu_bottom_admin;
    }

    @Override
    protected int selectedBottomNavItemId() {
        return R.id.nav_admin_more;
    }

    @Override
    protected String screenTitle() {
        return getString(R.string.admin_reports_title);
    }

    @Override
    protected boolean shouldShowToolbarActions() {
        return false;
    }

    @Override
    protected boolean shouldShowBackButton() {
        return true;
    }

    @Override
    protected boolean shouldUseAdminBackButtonStyling() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!session.isLoggedIn() || !DBHelper.ROLE_ADMIN.equals(session.getRole())) {
            session.clear();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        donHangDao = new DonHangDao(this);
        nguoiDungDao = new NguoiDungDao(this);

        loadReportData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReportData();
    }

    private void loadReportData() {
        if (donHangDao == null || nguoiDungDao == null) return;

        DonHangDao.ReportDashboardData dashboardData = donHangDao.getReportDashboardData();
        DonHangDao.ReportMetrics metrics = dashboardData.metrics;
        ArrayList<DonHangDao.MonthRevenue> revenueByMonth = dashboardData.revenueByMonth;
        ArrayList<DonHangDao.RecentMonthReport> recentSixMonths = dashboardData.recentSixMonths;
        ArrayList<DonHangDao.ProductSale> topProducts = donHangDao.getTopSanPhamBanChay(5);
        ArrayList<DonHangDao.StatusCount> orderStatuses = donHangDao.getSoDonTheoTrangThai();

        bindHeadlineKpis(metrics);
        bindLegacyKpis(metrics);
        bindSummary(metrics);

        LineChart chartRevenue = findViewById(R.id.chartRevenue);
        PieChart chartOrderStatus = findViewById(R.id.chartOrderStatus);
        BarChart chartRevenueProfit = findViewById(R.id.chartRevenueProfit);
        PieChart chartBestSellers = findViewById(R.id.chartBestSellers);

        renderRevenueByMonth(chartRevenue, revenueByMonth);
        renderOrderStatus(chartOrderStatus, orderStatuses);
        renderRevenueProfit(chartRevenueProfit, recentSixMonths);
        renderBestSellers(chartBestSellers, topProducts, metrics.deliveredUnits);
    }

    private void bindHeadlineKpis(DonHangDao.ReportMetrics metrics) {
        View cardRevenue = findViewById(R.id.cardRevenue);
        View cardProfit = findViewById(R.id.cardProfit);
        View cardUnits = findViewById(R.id.cardUnitsSold);
        View cardOrders = findViewById(R.id.cardOrders);

        bindKpiCard(cardRevenue, R.string.admin_reports_kpi_delivered_revenue, formatMoneyShort(metrics.recognizedRevenue));
        bindKpiCard(cardProfit, R.string.admin_reports_kpi_profit, formatMoneyShort(metrics.recognizedProfit));
        bindKpiCard(cardUnits, R.string.admin_reports_kpi_units, String.valueOf(metrics.deliveredUnits));
        bindKpiCard(cardOrders, R.string.admin_reports_kpi_orders, String.valueOf(metrics.deliveredPaidOrders));
    }

    private void bindLegacyKpis(DonHangDao.ReportMetrics metrics) {
        TextView tvRevenue = findViewById(R.id.tvRevenue);
        TextView tvOrders = findViewById(R.id.tvOrders);
        TextView tvCustomers = findViewById(R.id.tvCustomers);
        if (tvRevenue != null) {
            tvRevenue.setText(getString(R.string.kpi_revenue_value, formatMoney(metrics.recognizedRevenue)));
        }
        if (tvOrders != null) {
            tvOrders.setText(getString(R.string.kpi_orders_value, metrics.totalOrders));
        }
        if (tvCustomers != null) {
            tvCustomers.setText(getString(R.string.kpi_customers_value, nguoiDungDao.getCustomerCount()));
        }
    }

    private void bindSummary(DonHangDao.ReportMetrics metrics) {
        ((TextView) findViewById(R.id.tvReportTotalRevenue6m)).setText(formatMoneyCompact(metrics.recentSixMonthRevenue));
        ((TextView) findViewById(R.id.tvReportTotalProfit6m)).setText(formatMoneyCompact(metrics.recentSixMonthProfit));
        ((TextView) findViewById(R.id.tvReportTotalOrders)).setText(String.valueOf(metrics.recentSixMonthDeliveredPaidOrders));
        ((TextView) findViewById(R.id.tvReportAverageOrder)).setText(formatMoneyShort(metrics.recentSixMonthAverageOrderValue));
    }

    private void renderRevenueByMonth(LineChart chart, ArrayList<DonHangDao.MonthRevenue> raw) {
        chart.setNoDataText(getString(R.string.admin_reports_empty_revenue));
        chart.getDescription().setEnabled(false);

        int[] rev = new int[13];
        for (DonHangDao.MonthRevenue r : raw) {
            if (r.month >= 1 && r.month <= 12) rev[r.month] = r.revenue;
        }

        ArrayList<Entry> entries = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            entries.add(new Entry(m, rev[m]));
        }

        LineDataSet set = new LineDataSet(entries, getString(R.string.admin_reports_delivered_revenue_year_label));
        set.setLineWidth(2.5f);
        set.setCircleRadius(3.5f);
        set.setValueTextSize(10f);
        set.setColor(ContextCompat.getColor(this, R.color.admin_primary));
        set.setCircleColor(ContextCompat.getColor(this, R.color.admin_primary));
        set.setDrawFilled(true);
        set.setFillColor(ContextCompat.getColor(this, R.color.admin_surface_soft));

        LineData data = new LineData(set);
        chart.setData(data);
        chart.getAxisRight().setEnabled(false);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int m = (int) value;
                return (m >= 1 && m <= 12) ? "T" + m : "";
            }
        });

        chart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatAxisMillions((int) value);
            }
        });

        chart.invalidate();
    }

    private void renderOrderStatus(PieChart chart, ArrayList<DonHangDao.StatusCount> list) {
        chart.setNoDataText(getString(R.string.admin_reports_empty_status));
        chart.getDescription().setEnabled(false);
        chart.setDrawEntryLabels(false);

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (DonHangDao.StatusCount s : list) {
            entries.add(new PieEntry(s.count, OrdersAdapter.formatOrderStatus(this, s.status)));
        }

        PieDataSet set = new PieDataSet(entries, getString(R.string.admin_reports_status_label));
        set.setColors(
                ContextCompat.getColor(this, R.color.admin_primary),
                ContextCompat.getColor(this, R.color.admin_warning),
                ContextCompat.getColor(this, R.color.admin_success),
                ContextCompat.getColor(this, R.color.admin_danger)
        );
        set.setValueTextSize(11f);

        PieData data = new PieData(set);
        chart.setData(data);
        chart.invalidate();
    }

    private void renderRevenueProfit(BarChart chart, ArrayList<DonHangDao.RecentMonthReport> recentSixMonths) {
        chart.setNoDataText(getString(R.string.admin_reports_empty_revenue));
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setFitBars(true);

        ArrayList<BarEntry> revenueEntries = new ArrayList<>();
        ArrayList<BarEntry> profitEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        for (int index = 0; index < recentSixMonths.size(); index++) {
            DonHangDao.RecentMonthReport item = recentSixMonths.get(index);
            revenueEntries.add(new BarEntry(index, toMillions(item.revenue)));
            profitEntries.add(new BarEntry(index, toMillions(item.profit)));
            labels.add(item.label);
        }

        BarDataSet revenueSet = new BarDataSet(revenueEntries, getString(R.string.admin_reports_chart_revenue_label));
        revenueSet.setColor(ContextCompat.getColor(this, R.color.admin_dashboard_blue));
        BarDataSet profitSet = new BarDataSet(profitEntries, getString(R.string.admin_reports_chart_profit_label));
        profitSet.setColor(ContextCompat.getColor(this, R.color.admin_dashboard_green));
        revenueSet.setValueTextSize(10f);
        profitSet.setValueTextSize(10f);

        BarData data = new BarData(revenueSet, profitSet);
        data.setBarWidth(0.28f);
        chart.setData(data);
        chart.groupBars(-0.5f, 0.24f, 0.08f);
        chart.getXAxis().setAxisMinimum(-0.5f);
        chart.getXAxis().setAxisMaximum(Math.max(0.5f, recentSixMonths.size() - 0.5f + 1f));

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(12f);
        chart.invalidate();
    }

    private void renderBestSellers(PieChart chart, ArrayList<DonHangDao.ProductSale> topProducts, int totalUnits) {
        chart.setNoDataText(getString(R.string.admin_reports_empty_top_products));
        chart.getDescription().setEnabled(false);
        chart.setUsePercentValues(true);
        chart.setDrawEntryLabels(false);
        chart.setCenterText("");

        ArrayList<PieEntry> entries = new ArrayList<>();
        int totalQty = totalUnits;
        if (totalQty <= 0) {
            chart.clear();
            chart.invalidate();
            return;
        }

        int shownQty = 0;
        for (int i = 0; i < topProducts.size() && i < 4; i++) {
            DonHangDao.ProductSale product = topProducts.get(i);
            shownQty += Math.max(0, product.qty);
            entries.add(new PieEntry(product.qty, shortName(product.name)));
        }
        int otherQty = Math.max(0, totalQty - shownQty);
        if (otherQty > 0) {
            entries.add(new PieEntry(otherQty, "Khác"));
        }

        PieDataSet set = new PieDataSet(entries, getString(R.string.admin_reports_best_sellers_title));
        set.setSliceSpace(2f);
        set.setSelectionShift(4f);
        set.setColors(
                ContextCompat.getColor(this, R.color.admin_dashboard_blue),
                ContextCompat.getColor(this, R.color.admin_dashboard_green),
                ContextCompat.getColor(this, R.color.admin_dashboard_orange),
                ContextCompat.getColor(this, R.color.admin_dashboard_purple),
                ContextCompat.getColor(this, R.color.admin_dashboard_red)
        );
        PieData data = new PieData(set);
        data.setValueFormatter(new PercentFormatter(chart));
        data.setValueTextSize(12f);
        chart.setData(data);

        // Cấu hình Chú thích (Legend) để không bị tràn màn hình
        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setWordWrapEnabled(true); // Tự động xuống dòng nếu quá dài
        l.setXEntrySpace(10f);
        l.setYEntrySpace(5f);
        l.setYOffset(5f);
        l.setTextSize(12f);

        // Thêm khoảng cách lề dưới để chú thích hiện đầy đủ
        chart.setExtraOffsets(5, 5, 5, 15);
        chart.invalidate();
    }

    private void bindKpiCard(View card, int labelRes, String value) {
        if (card == null) {
            return;
        }
        ((TextView) card.findViewById(R.id.tvKpiLabel)).setText(labelRes);
        ((TextView) card.findViewById(R.id.tvKpiValue)).setText(value);
    }

    private float toMillions(int amount) {
        return amount / 1_000_000f;
    }

    private String formatAxisMillions(int amount) {
        return String.valueOf((int) Math.round(toMillions(amount)));
    }

    private String formatMoney(int amount) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + "đ";
    }

    private String formatMoneyShort(int amount) {
        boolean isNegative = amount < 0;
        long absAmount = Math.abs((long) amount);
        String formatted;
        if (absAmount >= 1_000_000_000) {
            formatted = trimDecimal(absAmount / 1_000_000_000f) + "B";
        } else if (absAmount >= 1_000_000) {
            formatted = trimDecimal(absAmount / 1_000_000f) + "M";
        } else if (absAmount >= 1_000) {
            formatted = trimDecimal(absAmount / 1_000f) + "K";
        } else {
            formatted = String.valueOf(absAmount);
        }
        return (isNegative ? "-" : "") + formatted;
    }

    private String formatMoneyCompact(int amount) {
        boolean isNegative = amount < 0;
        long absAmount = Math.abs((long) amount);
        String formatted;
        if (absAmount >= 1_000_000_000) {
            formatted = trimDecimal(absAmount / 1_000_000_000f) + " tỷ";
        } else if (absAmount >= 1_000_000) {
            formatted = trimDecimal(absAmount / 1_000_000f) + " triệu";
        } else {
            return formatMoney(amount);
        }
        return (isNegative ? "-" : "") + formatted;
    }

    private String trimDecimal(float value) {
        if (Math.abs(value - Math.round(value)) < 0.05f) {
            return String.valueOf(Math.round(value));
        }
        return String.format(Locale.US, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private String shortName(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.length() <= 14) return s;
        return s.substring(0, 14) + "…";
    }
}
