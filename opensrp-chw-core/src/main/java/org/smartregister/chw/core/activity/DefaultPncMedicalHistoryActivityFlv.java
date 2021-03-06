package org.smartregister.chw.core.activity;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.dao.PNCDao;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DefaultPncMedicalHistoryActivityFlv implements CorePncMedicalHistoryActivity.Flavor {

    protected LayoutInflater inflater;
    protected LinearLayout linearLayoutHealthFacilityVisit;
    protected LinearLayout linearLayoutHealthFacilityVisitDetails;
    protected TextView customFontTextViewLastVisit;
    protected LinearLayout linearLayoutLastVisit;
    protected LinearLayout linearLayoutPncHomeVisit;
    protected LinearLayout linearLayoutPncHomeVisitDetails;
    protected LinearLayout linearLayoutPncFamilyPlanning;
    protected LinearLayout linearLayoutPncFamilyPlanningDetails;
    protected LinearLayout linearLayoutPncChildVaccine;
    protected LinearLayout linearLayoutPncChildVaccineDetails;
    protected LinearLayout linearLayoutPncImmunization;
    protected LinearLayout linearLayoutPncImmunizationDetails;
    protected LinearLayout linearLayoutPncGrowthAndNutrition;
    protected LinearLayout linearLayoutPncGrowthAndNutritionDetails;


    @Override
    public View bindViews(Activity activity) {
        inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.medical_history_details, null);

        linearLayoutLastVisit = view.findViewById(R.id.linearLayoutLastVisit);
        customFontTextViewLastVisit = view.findViewById(R.id.customFontTextViewLastVisit);
        linearLayoutHealthFacilityVisit = view.findViewById(R.id.linearLayoutPncHealthFacilityVisit);
        linearLayoutHealthFacilityVisitDetails = view.findViewById(R.id.linearLayoutPncHealthFacilityVisitDetails);
        linearLayoutPncHomeVisit = view.findViewById(R.id.linearLayoutPncHomeVisit);
        linearLayoutPncHomeVisitDetails = view.findViewById(R.id.linearLayoutPncHomeVisitDetails);
        linearLayoutPncFamilyPlanning = view.findViewById(R.id.linearLayoutPncFamilyPlanning);
        linearLayoutPncFamilyPlanningDetails = view.findViewById(R.id.linearLayoutPncFamilyPlanningDetails);
        linearLayoutPncChildVaccine = view.findViewById(R.id.linearLayoutPncChildVaccine);
        linearLayoutPncChildVaccineDetails = view.findViewById(R.id.linearLayoutPncChildVaccineDetails);
        linearLayoutPncImmunization = view.findViewById(R.id.linearLayoutPncImmunization);
        linearLayoutPncImmunizationDetails = view.findViewById(R.id.linearLayoutPncImmunizationDetails);
        linearLayoutPncGrowthAndNutrition = view.findViewById(R.id.linearLayoutPncGrowthAndNutrition);
        linearLayoutPncGrowthAndNutritionDetails = view.findViewById(R.id.linearLayoutPncGrowthAndNutritionDetails);

        return view;
    }

    @Override
    public void processViewData(List<Visit> visits, Context context) {
        if (visits.size() > 0) {

            int days = 0;
            int x = 0;
            Map<String, Map<String, String>> healthFacility_visit = new HashMap<>();
            Map<String, String> family_planning = new HashMap<>();
            String vaccineCard = context.getString(R.string.pnc_no);
            String earlyBreastFeeding = "";
            Map<String, String> immunization = new HashMap<>();
            Map<String, String> growth_data = new HashMap<>();

            while (x < visits.size()) {
                // the first object in this list is the days difference
                if (x == 0) {
                    days = Days.daysBetween(new DateTime(visits.get(0).getDate()), new DateTime()).getDays();
                }
                x++;
            }
            // process the data
            for (Visit v : visits) {
                for (Map.Entry<String, List<VisitDetail>> entry : v.getVisitDetails().entrySet()) {
                    String val = getText(entry.getValue());

                    switch (entry.getKey()) {
                        // health facility
                        case "pnc_visit_1":
                        case "pnc_visit_2":
                        case "pnc_visit_3":

                            String date_key = "pnc_hf_visit1_date";
                            if (entry.getKey().equals("pnc_visit_2")) {
                                date_key = "pnc_hf_visit2_date";
                            }
                            if (entry.getKey().equals("pnc_visit_3")) {
                                date_key = "pnc_hf_visit3_date";
                            }

                            if ("Yes".equalsIgnoreCase(val)) {
                                Map<String, String> map = new HashMap<>();
                                // add details
                                map.put("pnc_hf_visit_date", getText(v.getVisitDetails().get(date_key)));
                                map.put("baby_weight", getText(v.getVisitDetails().get("baby_weight")));
                                map.put("baby_temp", getText(v.getVisitDetails().get("baby_temp")));
                                healthFacility_visit.put(entry.getKey(), map);
                            }
                            break;

                        // family planing
                        case "fp_method":
                        case "fp_start_date":
                            family_planning.put(getText(v.getVisitDetails().get("fp_method")), getText(v.getVisitDetails().get("fp_start_date")));
                            break;

                        // vaccine card
                        case "vaccine_card":
                            if ("No".equalsIgnoreCase(vaccineCard) && "Yes".equalsIgnoreCase(val)) {
                                vaccineCard = context.getString(R.string.pnc_yes);
                            }
                            break;
                        // immunization
                        case "opv0":
                        case "bcg":
                            immunization.put(entry.getKey(), val);
                            break;

                        // growth and nutrition
                        case "exclusive_breast_feeding":
                            growth_data.put(entry.getKey(), val);
                            break;
                    }
                }

                earlyBreastFeeding = PNCDao.earlyBreastFeeding(v.getBaseEntityId(),v.getVisitId());
                if(earlyBreastFeeding != null && earlyBreastFeeding.equalsIgnoreCase("Yes")){
                    earlyBreastFeeding = context.getString(R.string.pnc_yes);
                }
                else if(earlyBreastFeeding != null && earlyBreastFeeding.equalsIgnoreCase("No")) {
                    earlyBreastFeeding = context.getString(R.string.pnc_no);
                }
            }
            processLastVisit(days, context);
            processHealthFacilityVisit(healthFacility_visit, context);
            processFamilyPlanning(family_planning, context);
            processVaccineCard(vaccineCard, context);
            processImmunization(immunization, context);
            processGrowthAndNutrition(growth_data, context, earlyBreastFeeding);

        }
    }

    @NotNull
    protected String getText(@Nullable List<VisitDetail> visitDetails) {
        if (visitDetails == null) {
            return "";
        }
        List<String> vals = new ArrayList<>();
        for (VisitDetail vd : visitDetails) {
            String val = getText(vd);
            if (StringUtils.isNotBlank(val)) {
                vals.add(val);
            }
        }
        return toCSV(vals);
    }

    protected void processLastVisit(int days, Context context) {
        linearLayoutLastVisit.setVisibility(View.VISIBLE);
        customFontTextViewLastVisit.setText(StringUtils.capitalize(MessageFormat.format(context.getString(R.string.days_ago), String.valueOf(days))));
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
                if(entry.getValue().get("baby_weight") != null){
                    tvbabyWeight.setVisibility(View.VISIBLE);
                    tvbabyWeight.setText(context.getString(R.string.pnc_baby_weight, entry.getValue().get("baby_weight")));
                }
                if(entry.getValue().get("baby_temp") != null){
                    tvbabyTemp.setVisibility(View.VISIBLE);
                    tvbabyTemp.setText(context.getString(R.string.pnc_baby_temp, entry.getValue().get("baby_temp")));
                    linearLayoutHealthFacilityVisitDetails.addView(view, 0);
                }

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
                        case "Abstinence":
                            method = context.getString(R.string.pnc_abstinence);
                            break;
                        case "Condom":
                            method = context.getString(R.string.pnc_condom);
                            break;
                        case "Tablets":
                            method = context.getString(R.string.pnc_tablets);
                            break;
                        case "Injectable":
                            method = context.getString(R.string.pnc_injectable);
                            break;
                        case "IUD":
                            method = context.getString(R.string.pnc_iud);
                            break;
                        case "Implant":
                            method = context.getString(R.string.pnc_implant);
                            break;
                        case "Other":
                            method = context.getString(R.string.pnc_other);
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

    protected void processVaccineCard(String received, Context context) {
        if (received != null) {
            linearLayoutPncChildVaccine.setVisibility(View.VISIBLE);
            View view = inflater.inflate(R.layout.pnc_wcaro_child_vaccine_card, null);

            TextView tvPncVaccineCard = view.findViewById(R.id.pncVaccineCard);

            tvPncVaccineCard.setText(MessageFormat.format(context.getString(R.string.pnc_child_vaccine_card), received));
            linearLayoutPncChildVaccineDetails.addView(view, 0);
        }
    }

    protected void processImmunization(Map<String, String> immunization, Context context) {
        if ( immunization != null && immunization.size() > 0) {
            linearLayoutPncImmunization.setVisibility(View.VISIBLE);
            View view = inflater.inflate(R.layout.pnc_wcaro_immunization, null);

            TextView tvBirth = view.findViewById(R.id.pncBirth);
            tvBirth.setVisibility(View.VISIBLE);
            tvBirth.setText(context.getString(R.string.pnc_birth));
            for(Map.Entry<String, String> entry : immunization.entrySet()) {
                if(entry.getValue() != null) {
                    String entryValue = entry.getValue().equalsIgnoreCase("Vaccine not given") ? context.getString(R.string.pnc_vaccine_not_given) : entry.getValue();
                    if (entry.getKey().equals("bcg") ) {
                        TextView tvBcg = view.findViewById(R.id.pncBcg);
                        tvBcg.setVisibility(View.VISIBLE);
                        if(entryValue.equalsIgnoreCase(context.getString(R.string.pnc_vaccine_not_given)))
                            tvBcg.setText(MessageFormat.format(context.getString(R.string.pnc_bcg_not_done), entryValue));
                        else
                            tvBcg.setText(MessageFormat.format(context.getString(R.string.pnc_bcg), entryValue));
                    } else if (entry.getKey().equals("opv0")) {
                        TextView tvOpv0 = view.findViewById(R.id.pncOpv0);
                        tvOpv0.setVisibility(View.VISIBLE);
                        if(entryValue.equalsIgnoreCase(context.getString(R.string.pnc_vaccine_not_given)))
                            tvOpv0.setText(MessageFormat.format(context.getString(R.string.pnc_opv0_not_done), entryValue));
                        else
                            tvOpv0.setText(MessageFormat.format(context.getString(R.string.pnc_opv0), entryValue));
                    }
                }
            }
            linearLayoutPncImmunizationDetails.addView(view, 0);
        }
    }

    protected void processGrowthAndNutrition(Map<String, String> growth_data, Context context,String earlyBreastFeeding) {
        if (growth_data != null && growth_data.size() > 0) {
            linearLayoutPncGrowthAndNutrition.setVisibility(View.VISIBLE);
            View view = inflater.inflate(R.layout.pnc_wcaro_growth_and_nutrition, null);
            if(earlyBreastFeeding != null){
                TextView tvPncEarlyInitiationBf = view.findViewById(R.id.pncEarlyInitiationBf);
                tvPncEarlyInitiationBf.setVisibility(View.VISIBLE);
                tvPncEarlyInitiationBf.setText(MessageFormat.format(context.getString(R.string.pnc_early_initiation_bf), earlyBreastFeeding));
            }
            for (Map.Entry<String, String> entry : growth_data.entrySet()) {
                TextView tvpncExcussiveBf = view.findViewById(R.id.pncExcussiveBf);
                tvpncExcussiveBf.setVisibility(View.VISIBLE);
                if(entry.getValue().equalsIgnoreCase("YES")){
                    tvpncExcussiveBf.setText(MessageFormat.format(context.getString(R.string.pnc_exclusive_bf_0_months), context.getString(R.string.pnc_no)));
                }
                else {
                    tvpncExcussiveBf.setText(MessageFormat.format(context.getString(R.string.pnc_exclusive_bf_0_months), context.getString(R.string.pnc_yes)));
                }
            }
            linearLayoutPncGrowthAndNutritionDetails.addView(view, 0);
        }
    }

    /**
     * Extract value from VisitDetail
     *
     * @return
     */
    @NotNull
    protected String getText(@Nullable VisitDetail visitDetail) {
        if (visitDetail == null) {
            return "";
        }

        String val = visitDetail.getHumanReadable();
        if (StringUtils.isNotBlank(val)) {
            return val.trim();
        }

        return (StringUtils.isNotBlank(visitDetail.getDetails())) ? visitDetail.getDetails().trim() : "";
    }

    protected static String toCSV(List<String> list) {
        String result = "";
        if (list.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : list) {
                sb.append(s).append(",");
            }
            result = sb.deleteCharAt(sb.length() - 1).toString();
        }
        return result;
    }


}