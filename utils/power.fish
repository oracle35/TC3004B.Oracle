argparse 'status' -- $argv
or exit 

set -l cluster_id "ocid1.cluster.oc1.mx-queretaro-1.aaaaaaaabplu6aydw5bdtbc6yagkxfohtndyntqwcrfllrexyc3qlslhl4mq"

function oci
    command oci --config-file $PROJECT_ROOT/.oci/config $argv
end

function ocid_shorten
    string sub -s -8 $argv[1]
end

set data (
    oci compute instance list \
        --compartment-id $COMPARTMENT_OCID | jq -c --arg cluster_id $cluster_id '.data.[] | select(.metadata."oke-cluster-id" == $cluster_id) | { id: .id, state: ."lifecycle-state" }'
)

if set -q _flag_status
    for instance in $data
        set instance_id (echo $instance | jq -r '.id')
        set instance_state (echo $instance | jq -r '.state')
        echo "instance $(ocid_shorten $instance_id): $instance_state"
    end
    exit 0
end

function instance_action
    set instance_id (echo $argv[1] | jq -r '.id')
    switch $argv[2]
        case off
            set -f action SOFTSTOP
        case on
            set -f action START
        case restart
            set -f action SOFTRESET
        case '*'
            echo "unknown action $argv[1]"
            exit 1
    end
    echo "action $action on $(ocid_shorten $instance_id)..."
    oci compute instance action --instance-id "$instance_id" --action "$action" | jq '."lifecycle-state"'
end

for instance in $data
    instance_action $instance $argv[1]
end

