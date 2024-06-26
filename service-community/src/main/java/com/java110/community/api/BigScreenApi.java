package com.java110.community.api;

import com.java110.community.bmo.assets.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/bigScreen")
public class BigScreenApi {


    @Autowired
    private IQueryAssetsBMO queryAssetsBMOImpl;

    @Autowired
    private IQueryAssetsRoomBMO queryAssetsRoomBMOImpl;

    @Autowired
    private IQueryAssetsRepairBMO queryAssetsRepairBMOImpl;

    @Autowired
    private IQueryAssetsFeeBMO queryAssetsFeeBMOImpl;

    @Autowired
    private IQueryAssetsOpenDoorBMO queryAssetsOpenDoorBMOImpl;


    /**
     * 查询资产
     *
     * @param communityId
     * @return
     * @Service /bigScreen/getAssets
     * @path /app/bigScreen/getAssets
     */
    @RequestMapping(value = "/getAssets", method = RequestMethod.GET)
    public ResponseEntity<String> getAssets(@RequestParam(value = "communityId") String communityId) {
        return queryAssetsBMOImpl.query(communityId);
    }


    /**
     * 查询房屋
     *
     * @param communityId
     * @return
     * @Service /bigScreen/getAssetsRoom
     * @path /app/bigScreen/getAssetsRoom
     */
    @RequestMapping(value = "/getAssetsRoom", method = RequestMethod.GET)
    public ResponseEntity<String> getAssetsRoom(@RequestParam(value = "communityId") String communityId) {
        return queryAssetsRoomBMOImpl.query(communityId);
    }

    /**
     * @param communityId
     * @return
     * @Service /bigScreen/getAssetsRepair
     * @path /app/bigScreen/getAssetsRepair
     */
    @RequestMapping(value = "/getAssetsRepair", method = RequestMethod.GET)
    public ResponseEntity<String> getAssetsRepair(@RequestParam(value = "communityId") String communityId) {
        return queryAssetsRepairBMOImpl.query(communityId);
    }

    /**
     * 查询大屏费用
     *
     * @param communityId
     * @return
     * @Service /bigScreen/getAssetsFee
     * @path /app/bigScreen/getAssetsFee
     */
    @RequestMapping(value = "/getAssetsFee", method = RequestMethod.GET)
    public ResponseEntity<String> getAssetsFee(@RequestParam(value = "communityId") String communityId) {
        return queryAssetsFeeBMOImpl.query(communityId);
    }

    /**
     * 开门
     *
     * @param communityId
     * @return
     * @Service /bigScreen/getAssetOpenDoor
     * @path /app/bigScreen/getAssetOpenDoor
     */
    @RequestMapping(value = "/getAssetOpenDoor", method = RequestMethod.GET)
    public ResponseEntity<String> getAssetOpenDoor(@RequestParam(value = "communityId") String communityId) {
        return queryAssetsOpenDoorBMOImpl.query(communityId);
    }

    @RequestMapping(value = "/getAssetOpenDoorImage", method = RequestMethod.GET)
    public ResponseEntity<String> getAssetOpenDoorImage(@RequestParam(value = "communityId") String communityId) {
        return queryAssetsOpenDoorBMOImpl.queryImage(communityId);

    }
}
