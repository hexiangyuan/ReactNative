package com.daigou.selfstation.pick.presenter;

import com.android.volley.Response;
import com.daigou.model.RpcRequest;
import com.daigou.selfstation.pick.model.IPickModel;
import com.daigou.selfstation.pick.model.PickModelImpl;
import com.daigou.selfstation.pick.view.IPickView;
import com.daigou.selfstation.rpc.selfstation.TDeliveryMethod;
import com.daigou.selfstation.rpc.selfstation.TFindSubPackageResult;
import com.daigou.selfstation.rpc.selfstation.TSaveResult;
import com.daigou.selfstation.rpc.selfstation.TSaveSubPkgInfo;

import java.util.ArrayList;

/**
 * Created by 何祥源 on 16/4/28.
 * Desc:
 */
public class PickPresenterImpl implements IPickPresenter {
    private IPickModel model;
    private IPickView view;

    public PickPresenterImpl(IPickView view) {
        this.model = new PickModelImpl();
        this.view = view;
    }

    @Override
    public void showProgress() {
        view.showProgressBar();
    }

    @Override
    public void hideProgress() {
        view.hideProgressBar();
    }

    @Override
    public void search(String deliveryCode, String startDate, String endDate, boolean isPicked, String driverOrStation, String periodOrNeighborhoodStation, int house, String AMPM) {
        showProgress();
        model.search(deliveryCode, startDate, endDate, isPicked, driverOrStation, periodOrNeighborhoodStation, house, AMPM, new Response.Listener<TFindSubPackageResult>() {
            @Override
            public void onResponse(TFindSubPackageResult response) {
                view.searchClicked(response);
                if (response != null) {
                } else {
                    view.showError();
                }
                hideProgress();
            }
        });
    }


    @Override
    public void pickedChecked() {
        view.isPickChecked();
    }

    @Override
    public void deliveryMethodClicked() {
        view.showDeliveryMethod();
    }

    @Override
    public void startTimeClicked() {
        view.showStartTime();
    }

    @Override
    public void endTimeClicked() {
        view.showEndTime();
    }

    @Override
    public void isLock(final ArrayList<Integer> list, final boolean isLock) {
        model.setParcelsIsLocked(list, isLock, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    view.lockSucceeded(isLock, list);
                } else {
                    view.showError();
                }
            }
        });

    }

    @Override
    public void showAllPeriod(String stationName) {
        showProgress();
        model.loadPeriod(stationName, new Response.Listener<ArrayList<String>>() {
            @Override
            public void onResponse(ArrayList<String> response) {
                hideProgress();
                if (response != null && response.size() > 0) {
                    view.showPeriod(response);
                }
            }
        });
    }

    @Override
    public void showNeighborRegion() {
        view.showRegion();
    }

    @Override
    public void showNeighborhoodStation(String stationName) {
        showProgress();
        model.loadNeighborhoodStations(stationName, new Response.Listener<ArrayList<String>>() {
            @Override
            public void onResponse(ArrayList<String> response) {
                hideProgress();
                if (response != null) {
                    view.showNeighborhoodStations(response);
                }
            }
        });
    }

    @Override
    public void showAllDriver() {
        showProgress();
        model.loadDriver(new Response.Listener<ArrayList<String>>() {
            @Override
            public void onResponse(ArrayList<String> response) {
                if (response != null) {
                    view.showDrivers(response);
                } else {
                    view.showError();
                }
                hideProgress();
            }
        });
    }

    @Override
    public void showHouses() {
        showProgress();
        model.loadHouses(new Response.Listener<ArrayList<String>>() {
            @Override
            public void onResponse(ArrayList<String> response) {
                if (response != null) {
                    view.showHouses(response);
                }
                hideProgress();
            }
        });
    }

    @Override
    public void showMRTStations(boolean addAM) {
        showProgress();
        model.loadMRTStations(addAM, new Response.Listener<ArrayList<String>>() {
            @Override
            public void onResponse(ArrayList<String> response) {
                if (response != null) {
                    view.showMRTStation(response);
                }
                hideProgress();
            }
        });
    }

    @Override
    public void showSelfCollectionStations() {
        view.showSelfCollections();
    }

    @Override
    public void checkAll() {
        view.checkAll();
    }

    @Override
    public void clearCheck() {
        view.clearCheck();
    }

    @Override
    public void loadDeliveryMethod() {
        showProgress();
        model.loadDeliveryMethod(new Response.Listener<ArrayList<TDeliveryMethod>>() {
            @Override
            public void onResponse(ArrayList<TDeliveryMethod> response) {
                if (response != null) {
                    hideProgress();
                    view.methodLoadFinished(response);
                }
            }
        });
    }

    @Override
    public void save(String subPkg, String BP, String boxNo, int pkgId, int shipmentId, boolean isForceSave) {
        showProgress();
        model.save(subPkg, BP, pkgId, shipmentId, boxNo, isForceSave, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgress();
                if (response != null) {
                    view.saveClicked(response);
                }
            }
        });
    }

    @Override
    public void saveSubPkgs(String boxNum, ArrayList<TSaveSubPkgInfo> pkgInfos) {
        showProgress();
        model.saveSubPkgs(boxNum, pkgInfos, new Response.Listener<TSaveResult>() {
            @Override
            public void onResponse(TSaveResult response) {
                hideProgress();
                if (response != null) {
                    view.showSavedMsg(response);
                } else {
                    view.showError();
                }
            }
        });
    }

    @Override
    public void userGetNeighborhoodRegions(boolean addAm) {
        showProgress();
        model.UserGetNeighborhoodStations(addAm, new Response.Listener<ArrayList<String>>() {
            @Override
            public void onResponse(ArrayList<String> response) {
                hideProgress();
                if (response != null) {
                    view.neighborhoodRegionsLoaded(response);
                } else {
                    view.showError();
                }
            }
        });
    }

    @Override
    public void cancelRpcRequests() {
        if (model.requests.size() > 0) {
            for (RpcRequest request : model.requests) {
                request.cancel();
            }
        }
    }
}
