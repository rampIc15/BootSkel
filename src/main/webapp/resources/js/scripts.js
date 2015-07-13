// Empty JS for your own code to be here

window.operateEvents = {
    'click .like': function (e, value, row, index) {
        alert('You click like icon, row: ' + JSON.stringify(row));
        console.log(value, row, index);
    },
    'click .edit': function (e, value, row, index) {
        alert('You click edit icon, row: ' + JSON.stringify(row));
        console.log(value, row, index);
    },
    'click .remove': function (e, value, row, index) {
        alert('You click remove icon, row: ' + JSON.stringify(row));
        console.log(value, row, index);
    }
};
var UIActions = {

    init: function () {
        this.bindUIControls();


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
                    title: 'Operations',
                    align: 'center',
                    valign: 'middle',
                    clickToSelect: false,
                    formatter: this.tableOperColFormater,
                    events: operateEvents
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
            '<a class="like" href="javascript:void(0)" title="Like">',
            '<i class="glyphicon glyphicon-heart"></i>',
            '</a>',
            '<a class="edit ml10" href="javascript:void(0)" title="Edit">',
            '<i class="glyphicon glyphicon-edit"></i>',
            '</a>',
            '<a class="remove ml10" href="javascript:void(0)" title="Remove">',
            '<i class="glyphicon glyphicon-remove"></i>',
            '</a>'
        ].join('');
    }

}
