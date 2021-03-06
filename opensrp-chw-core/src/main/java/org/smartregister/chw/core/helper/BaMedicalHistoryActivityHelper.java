package org.smartregister.chw.core.helper;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.activity.DefaultPncMedicalHistoryActivityFlv;
import org.smartregister.dao.AbstractDao;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class BaMedicalHistoryActivityHelper extends DefaultPncMedicalHistoryActivityFlv {
    private Date DeliveryDateFormatted;
    private String VisitDateFormattedString;

    public void processViewData(List<Visit> visits, Context context) {
        super.processViewData(visits, context);
        Map<Integer, String> home_visits = new LinkedHashMap<>();
        Map<String, String> hf_visits = new LinkedHashMap<>();
        int x = 0;
        while (x < visits.size()) {
            exctractHFVisits(visits, hf_visits, x);
            x++;
        }
        exctractHomeVisits(visits, home_visits);
        processHomeVisits(home_visits, context);
        processHealthFVisit(hf_visits, context);
    }

    protected void exctractHFVisits(List<Visit> sourceVisits, Map<String, String> hf_visits, int iteration) {
        int x = 1;
        while (x <= 4) {
            if(sourceVisits != null){
                List<VisitDetail> hf_details = sourceVisits.get(iteration).getVisitDetails().get("pnc_visit_" + x);
                if (hf_details != null) {
                    for (VisitDetail D : hf_details
                    ) {
                        if (D.getHumanReadable().equalsIgnoreCase("Yes")) {

                            List<VisitDetail> hf_details_dates = sourceVisits.get(iteration).getVisitDetails().get("pnc_hf_visit" + x + "_date");

                            if (hf_details_dates != null) {
                                for (VisitDetail hfDate : hf_details_dates) {
                                    hf_visits.put(D.getVisitKey(), hfDate.getDetails());
                                }
                            }
                        }
                    }
                }
                x++;
            }
            }
    }

    protected void exctractHomeVisits(List<Visit> sourceVisits, Map<Integer, String> home_visits) {
        if (sourceVisits != null) {
            String id = sourceVisits.get(0).getBaseEntityId();
            if (id != null) {
                String DeliveryDateSql = "SELECT delivery_date FROM ec_pregnancy_outcome where base_entity_id = ? ";
                List<Map<String, Object>> valus = AbstractDao.readData(DeliveryDateSql, new String[]{id});
                if (valus.size() > 0) {
                    String deliveryDate = valus.get(0).get("delivery_date").toString();
                    for (Visit vst : sourceVisits
                    ) {
                        try {
                            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                            DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                            DeliveryDateFormatted = dateFormat.parse(deliveryDate);
                            VisitDateFormattedString = dateFormat1.format(vst.getDate());
                        } catch (ParseException e) {
                            Timber.e(e, e.toString());
                        }
                        int res = Days.daysBetween((new DateTime(DeliveryDateFormatted)), new DateTime(vst.getDate())).getDays();
                        home_visits.put(res, VisitDateFormattedString);
                    }
                }
            }
        }
    }

    protected void processHomeVisits(Map<Integer, String> home_visits, Context context) {
        if (home_visits != null && home_visits.size() > 0) {
            linearLayoutPncHomeVisit.setVisibility(View.VISIBLE);

            for (Map.Entry<Integer, String> entry : home_visits.entrySet()) {
                View view = inflater.inflate(R.layout.medical_history_pnc_visit, null);

                TextView tvTitle = view.findViewById(R.id.pnctitle);
                if (entry.getKey() <= 2) {
                    tvTitle.setText(MessageFormat.format(context.getString(R.string.pnc_visit_date), context.getString(R.string.pnc_home_day_one_visit), entry.getValue()));
                }
                if ((entry.getKey() > 2) && (entry.getKey() <= 3)) {
                    tvTitle.setText(MessageFormat.format(context.getString(R.string.pnc_visit_date), context.getString(R.string.pnc_home_day_three_visit), entry.getValue()));
                }
                if ((entry.getKey() > 3) && (entry.getKey() <= 8)) {
                    tvTitle.setText(MessageFormat.format(context.getString(R.string.pnc_visit_date), context.getString(R.string.pnc_home_day_eight_visit), entry.getValue()));
                }
                if ((entry.getKey() > 8) && (entry.getKey() <= 27)) {
                    tvTitle.setText(MessageFormat.format(context.getString(R.string.pnc_visit_date), context.getString(R.string.pnc_home_day_twenty_one_to_twenty_seven_visit), entry.getValue()));
                }
                if ((entry.getKey() > 27) && (entry.getKey() <= 42)) {
                    tvTitle.setText(MessageFormat.format(context.getString(R.string.pnc_visit_date), context.getString(R.string.pnc_home_day_thirty_five_to_forty_one_visit), entry.getValue()));
                }

                linearLayoutPncHomeVisitDetails.addView(view, 0);

            }
        }

    }

    protected void processHealthFVisit(Map<String, String> hf_visits, Context context) {
        if (hf_visits != null && hf_visits.size() > 0) {
            linearLayoutHealthFacilityVisit.setVisibility(View.VISIBLE);

            for (Map.Entry<String, String> entry : hf_visits.entrySet()) {
                View view = inflater.inflate(R.layout.medical_history_pnc_visit, null);

                TextView tvTitle = view.findViewById(R.id.pnctitle);
                if (entry.getKey().equalsIgnoreCase("pnc_visit_3")) {
                    tvTitle.setText(MessageFormat.format(context.getString(R.string.pnc_visit_date), context.getString(R.string.pnc_eight_to_twenty_eight), entry.getValue()));
                }
                if (entry.getKey().equalsIgnoreCase("pnc_visit_1")) {
                    tvTitle.setText(MessageFormat.format(context.getString(R.string.pnc_visit_date), context.getString(R.string.pnc_48_hours), entry.getValue()));
                }
                if (entry.getKey().equalsIgnoreCase("pnc_visit_2")) {
                    tvTitle.setText(MessageFormat.format(context.getString(R.string.pnc_visit_date), context.getString(R.string.pnc_three_to_seven_days), entry.getValue()));
                }
                if (entry.getKey().equalsIgnoreCase("pnc_visit_4")) {
                    tvTitle.setText(MessageFormat.format(context.getString(R.string.pnc_visit_date), context.getString(R.string.pnc_twenty_nine_to_forty_two), entry.getValue()));
                }
                linearLayoutHealthFacilityVisitDetails.addView(view, 0);
            }


        }
    }

    protected void processFamilyPlanning(Map<String, String> family_plnning, Context context) {
        if (family_plnning != null && family_plnning.size() > 0) {
            linearLayoutPncFamilyPlanning.setVisibility(View.VISIBLE);
            for (Map.Entry<String, String> entry : family_plnning.entrySet()
            ) {
                View view = inflater.inflate(R.layout.pnc_wcaro_family_planning, null);
                if(entry.getKey() != null){
                    TextView tvPncFamilyPlanningMethod = view.findViewById(R.id.pncFamilyPlanningMethod);
                    String method = "";
                    switch (entry.getKey()){
                        case "None":
                            method = context.getString(R.string.pnc_none);
                            break;
                        case "PPIUCD":
                            method = context.getString(R.string.ppiucd);
                            break;
                        case "Pills":
                            method = context.getString(R.string.pills);
                            break;
                        case "Condoms":
                            method = context.getString(R.string.condoms);
                            break;
                        case "LAM":
                            method = context.getString(R.string.lam);
                            break;
                        case "Bead Counting":
                            method = context.getString(R.string.standard_day_method);
                            break;
                        case "Permanent (BTL)":
                            method = context.getString(R.string.permanent_blt);
                            break;
                        case "Permanent (Vasectomy)":
                            method = context.getString(R.string.permanent_vasectomy);
                            break;
                    }
                    tvPncFamilyPlanningMethod.setVisibility(View.VISIBLE);
                    tvPncFamilyPlanningMethod.setText(MessageFormat.format(context.getString(R.string.pnc_family_planning_method),method));
                }
                if(entry.getValue() != null){
                    TextView tvPncFamilyPlanningDate = view.findViewById(R.id.pncFamilyPlanningDate);
                    tvPncFamilyPlanningDate.setVisibility(View.VISIBLE);
                    tvPncFamilyPlanningDate.setText(MessageFormat.format(context.getString(R.string.pnc_family_planning_date), entry.getValue()));
                }
                linearLayoutPncFamilyPlanningDetails.addView(view, 0);
            }
        }
    }

    protected void processHealthFacilityVisit(Map<String, Map<String, String>> healthFacility_visit, Context context) {
        if (healthFacility_visit != null && healthFacility_visit.size() > 0) {
            linearLayoutHealthFacilityVisit.setVisibility(View.VISIBLE);

            for (Map.Entry<String, Map<String, String>> entry : healthFacility_visit.entrySet()) {
                View view = inflater.inflate(R.layout.pnc_wcaro_health_facility_visit, null);

                TextView tvbabyWeight = view.findViewById(R.id.babyWeight);
                TextView tvTitle = view.findViewById(R.id.pncHealthVisit);
                TextView tvbabyTemp = view.findViewById(R.id.babyTemp);
                tvTitle.setText(MessageFormat.format(context.getString(R.string.pnc_wcaro_health_facility_visit), entry.getValue().get("pnc_hf_visit_date")));
                if(entry.getValue().get("baby_weight") != null)
                    tvbabyWeight.setVisibility(View.GONE);

                if(entry.getValue().get("baby_temp") != null)
                    tvbabyTemp.setVisibility(View.GONE);
                tvbabyTemp.setText(context.getString(R.string.pnc_baby_temp, entry.getValue().get("baby_temp")));
                linearLayoutHealthFacilityVisitDetails.addView(view, 0);
            }
        }
    }
}
