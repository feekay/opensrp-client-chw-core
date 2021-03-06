package org.smartregister.chw.core.interactor;

import android.content.Context;

import org.smartregister.chw.anc.contract.BaseAncMedicalHistoryContract;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.dao.VisitDao;
import org.smartregister.chw.pnc.interactor.BasePncMedicalHistoryInteractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CorePncMedicalHistoryActivityInteractor extends BasePncMedicalHistoryInteractor {

    @Override
    public void getMemberHistory(final String memberID, final Context context, final BaseAncMedicalHistoryContract.InteractorCallBack callBack) {
        final Runnable runnable = () -> {

            List<Visit> visits = VisitDao.getPNCVisitsMedicalHistory(memberID);

            List<VisitDetail> detailList = VisitDao.getPNCMedicalHistory(memberID);
            Map<String, List<VisitDetail>> detailsMap = new HashMap<>();
            if (detailList != null) {
                for (VisitDetail d : detailList) {
                    List<VisitDetail> currentDetails = detailsMap.get(d.getVisitId());
                    if (currentDetails == null) currentDetails = new ArrayList<>();

                    currentDetails.add(d);
                    detailsMap.put(d.getVisitId(), currentDetails);
                }
            }

            if ( visits.size() > 0) {
                for(int x = 0; x < visits.size(); x++){
                    String visitID = visits.get(x).getVisitId();
                    List<VisitDetail> idDetails = detailsMap.get(visitID);
                    if (idDetails != null)
                        visits.get(x).setVisitDetails(VisitUtils.getVisitGroups(idDetails));
                }

            }

            appExecutors.mainThread().execute(() -> callBack.onDataFetched(visits));
        };

        appExecutors.diskIO().execute(runnable);
    }
}
