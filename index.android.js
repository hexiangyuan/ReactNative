'use strict';

import React from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  ListView,
  Image,
  Dimensions,
  TouchableHighlight,
  NativeModules,
  ToolbarAndroid,
} from 'react-native';
import ToastAndroid from './js/ToastAndroid';
import StartActivityAndroid from './js/StartActivityAndroid';
class HelloWorld extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      data: [],
      ds: new ListView.DataSource(
        {
          rowHasChanged: (r1, r2) => r1 !== r2,
        }
      )
    };
  }

  componentDidMount() {
    StartActivityAndroid.dataToJS((msg) => {
      const icons = [];
      switch (msg) {
        case "delivery staff":
          icons.push({ icon: "ic_scan", title: "Scan to shelf" });
          icons.push({ icon: "ic_picking", title: "Picking" });
          icons.push({ icon: "ic_search", title: "Search" });
          icons.push({ icon: "ic_search_scan", title: "Search scan" });
          icons.push({ icon: "ic_ready_for_collection", title: "Ready for collection" });
          icons.push({ icon: "ic_delivery", title: "Scan for delivery" });
          icons.push({ icon: "ic_neighborhood_picking", title: "Neighborhood picking" });
          icons.push({ icon: "ic_d2d", title: "Home delivery" });
          icons.push({ icon: "ic_driver_pack", title: "Loading goods" });
          icons.push({ icon: "ic_setting", title: "Setting" });
          icons.push({ icon: "ic_update", title: "Update" });
          icons.push({ icon: "ic_log_out", title: "Logout" });
          break;
      }

      this.setState({
        data: icons,
      });

    }, (errorMsg) => { })
  }

   _onPressRow(title: string) {
    switch (title) {
      case "Scan to shelf":
        StartActivityAndroid.startActivityFromJSExtras(
          "com.daigou.selfstation.activity.ScanActivity",
          { from: "ScanToShelf" });
        break;
      case "Picking":
        StartActivityAndroid.startActivityFromJS("com.daigou.selfstation.pick.ui.PickingActivity");
        break;
      case "Search":
        StartActivityAndroid.startActivityFromJS("com.daigou.selfstation.activity.SearchParcelActivity");
        break;
      case "Search scan":
        StartActivityAndroid.startActivityFromJS("com.daigou.selfstation.activity.SearchSubParcelActivity");
        break;
      case "Ready for collection":
        StartActivityAndroid.startActivityFromJSExtras("com.daigou.selfstation.activity.ScanActivity",
          { from: "ReadyForCollection" });
        break;
      case "Scan for delivery":
        StartActivityAndroid.startActivityFromJSExtras("com.daigou.selfstation.activity.ScanActivity",
          { from: "Out4Delivery" });
        break;
      case "Neighborhood picking":
        StartActivityAndroid.startActivityFromJS("com.daigou.selfstation.nhpicking.NHPickingActivity");
        break;
      case "Home delivery":
        StartActivityAndroid.startActivityFromJS("com.daigou.selfstation.d2d.D2dListActivity");
        break;
      case "Loading goods":
        StartActivityAndroid.startActivityFromJS("com.daigou.selfstation.d2d.DriverQueueActivity");
        break;
      case "Setting":
        StartActivityAndroid.startActivityFromJS("com.daigou.selfstation.activity.MySettingActivity");
        break;
      case "Update":
        NativeModules.IntentAndroid.openURL("https://play.google.com/store/apps/details?id=com.ezbuy.ezbiz");
        break;
      case "Logout":
        StartActivityAndroid.startActivityFromJS("com.daigou.selfstation.activity.ScanActivity");
        break;
    }
  }

  render() {
    return (
      <View>
        <ToolbarAndroid
          style={styles.toolbar}
          title="ezbiz" titleColor="#FFF" />
        <ListView contentContainerStyle={styles.list}
          dataSource={this.state.ds.cloneWithRows(this.state.data)}
          initialListSize={15}
          enableEmptySections={true}
          renderRow={(rowData, sectionID, rowID) =>
            <TouchableHighlight
              style={styles.hightLight}
              underlayColor="#999999"
              onPress={() => { this._onPressRow(rowData.title) } }>
              <View style={styles.row}>
                <Image style={styles.image} source={{ uri: rowData.icon }} />
                <Text style={styles.text}>{rowData.title}</Text>
              </View>
            </TouchableHighlight>
          }
          />
      </View>
    );
  }
}


let styles = StyleSheet.create({
  list: {
    flexDirection: 'row',
    flexWrap: 'wrap'
  },
  hightLight: {
    backgroundColor: 'white',
    width: Dimensions.get('window').width / 3,
    height: Dimensions.get('window').width / 3,
    borderWidth: 0.5,
    borderColor: "#CCC",

  },
  row: {
    width: Dimensions.get('window').width / 3,
    height: Dimensions.get('window').width / 3,
    alignItems: 'center',
    justifyContent: 'center'
  },

  text: {
    fontSize: 14,
    textAlign: 'center'
  },
  image: {
    width: 32,
    height: 32,
  },
  toolbar: {
    backgroundColor: '#3E82F7',
    height: 56,
  },
});

AppRegistry.registerComponent('HelloWorld', () => HelloWorld);
