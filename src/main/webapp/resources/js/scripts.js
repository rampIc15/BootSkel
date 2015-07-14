// Empty JS for your own code to be here

window.operateEvents = {
    'click .remove': function (e, value, row, index) {
        alert('Shutdown ? : ' + JSON.stringify(row));
        console.log(value, row, index);
    }
};
var UIActions = {

    init: function () {
        this.bindUIControls();

        //NODE DATA TABLE
        $('#node-data-table').bootstrapTable({
            data: [],
            columns: [
                {
                    field: 'name',
                    title: 'Node Name'
                },
                {
                    field: 'ip',
                    title: 'IP Address'
                },
                {
                    field: 'operate',
                    title: 'Shutdown',
                    align: 'center',
                    valign: 'middle',
                    clickToSelect: false,
                    formatter: this.tableOperColFormater,
                    events: operateEvents
                }
            ]
        });

        //INDICES TABLE
        $('#indexTable').bootstrapTable({
            method: 'get',
            url: '/indicesInfo',
            cache: false,
            height: 400,
            striped: true,
            pagination: true,
            pageSize: 50,
            pageList: [10, 25, 50, 100, 200],
            search: true,
            showColumns: true,
            showRefresh: true,
            minimumCountColumns: 2,
            clickToSelect: true,
            columns: [
                {
                    field: 'state',
                    checkbox: true
                },
                {
                    field: 'index',
                    title: 'Index Name',
                    align: 'right',
                    valign: 'bottom',
                    sortable: true
                },
                {
                    field: 'primaryreplicas',
                    title: 'Primary / Replicas',
                    align: 'center',
                    valign: 'middle',
                    sortable: true,
//                    formatter: nameFormatter
                },
                {
                    field: 'docsCount',
                    title: 'No.Of Docs',
                    align: 'left',
                    valign: 'top',
                    sortable: true,
//                    formatter: priceFormatter,
//                    sorter: priceSorter
                },
                {
                    field: 'size',
                    title: 'Index Size',
                    align: 'center',
                    valign: 'middle',
                    clickToSelect: false,
//                    formatter: operateFormatter,
//                    events: operateEvents
                }
            ]
        });

    },
    //Clicking on the Cluster Tab..
    bindUIControls: function () {
        var _this = this;
        $('#clusterTab').click(
            function (e) {
                $.get('/nodesInfo', function (data) {
                    _this.buildNodeDataTable(data);
                });

                //populate cluster badges
                $.get('/clusterHealth', function (data) {
                    var jdata = $.parseJSON(data);
                    $('#clusterHeading').text(jdata.cluster_name)
                    $('#nodeBadge').text(jdata.number_of_nodes);
                    $('#dateNodeBadge').text(jdata.number_of_data_nodes);
                    $('#activeShardsBadge').text(jdata.active_shards);
                    $('#primaryShardsBadge').text(jdata.active_primary_shards);

                });
            }
        );
    },


    //Build Node Data Table
    buildNodeDataTable: function (ajaxData) {
        var jdata = $.parseJSON(ajaxData);

        nodeNameArr = this.utilGetObjectKeys(jdata.nodes);
        var tableUiDataArr = [];
        for (var i in nodeNameArr) {
            var tableUiData = {};
            tableUiData["name"] = jdata.nodes[nodeNameArr[i]].name;
            tableUiData["ip"] = jdata.nodes[nodeNameArr[i]].ip;
            tableUiDataArr.push(tableUiData);
        }
        $('#node-data-table').bootstrapTable("load", tableUiDataArr);

    },

    utilGetObjectKeys: function (obj) {
        var keys = []
        for (var k in obj) {
            if (!obj.hasOwnProperty(k))
                continue
            keys.push(k)
        }
        return keys
    },

    tableOperColFormater: function operateFormatter(value, row, index) {
        return [
            '<a class="remove ml10" href="javascript:void(0)" title="Remove">',
            '<i class="glyphicon glyphicon-off" style="color:red"></i>',
            '</a>'
        ].join('');
    }

}
