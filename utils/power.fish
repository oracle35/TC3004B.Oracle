argparse 'status' -- $argv
or exit 

set data (
    oci --config-file $PROJECT_ROOT/.oci/config \
        compute instance list \
        --compartment-id $COMPARTMENT_OCID | jq -c '.data.[] | { id: .id, state: ."lifecycle-state" }'
)

if set -q _flag_status
    for instance in $data
        set instance_id (echo $instance | jq -r '.id')
        set instance_state (echo $instance | jq -r '.state')
        echo "instance $instance_id: $instance_state"
    end
end

