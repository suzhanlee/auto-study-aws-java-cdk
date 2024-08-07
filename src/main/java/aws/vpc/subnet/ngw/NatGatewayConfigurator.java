package aws.vpc.subnet.ngw;


import static aws.vpc.type.SubnetType.PUBLIC_TYPE;

import aws.vpc.subnet.dto.NatGatewayDto;
import aws.vpc.subnet.dto.SubnetDto;
import aws.vpc.util.TagUtils;
import software.amazon.awscdk.services.ec2.CfnEIP;
import software.amazon.awscdk.services.ec2.CfnEIP.Builder;
import software.amazon.awscdk.services.ec2.CfnNatGateway;
import software.constructs.Construct;

public class NatGatewayConfigurator {

    private static final String EIP = "EIP1";
    private static final String ID_PREFIX = "NateGateWay";

    private final Construct scope;

    public NatGatewayConfigurator(Construct scope) {
        this.scope = scope;
    }

    public NatGatewayDto configure(SubnetDto subnetDto) {
        if (isPublicType(subnetDto)) {
            return createNatGatewayDto(subnetDto);
        }
        throw new RuntimeException("Natgateway를 생성하려 하는 서브넷이 private 입니다.");
    }

    private boolean isPublicType(SubnetDto subnetDto) {
        return PUBLIC_TYPE.equals(subnetDto.type());
    }

    private NatGatewayDto createNatGatewayDto(SubnetDto subnetDto) {
        int suffix = createSuffix(subnetDto);
        CfnNatGateway natGateway = createNatGateway(subnetDto.id(), createEIP(EIP + suffix), ID_PREFIX + suffix);
        return new NatGatewayDto(natGateway.getAttrNatGatewayId());
    }

    private int createSuffix(SubnetDto subnetDto) {
        if (subnetDto.az().existsFirstAZ()) {
            return 1;
        }
        return 2;
    }

    private CfnNatGateway createNatGateway(String subnetId, CfnEIP eip, String id) {
        CfnNatGateway natGateway = CfnNatGateway.Builder.create(scope, id)
                .subnetId(subnetId)
                .allocationId(eip.getAttrAllocationId())
                .build();
        TagUtils.applyTags(natGateway);
        return natGateway;
    }

    private CfnEIP createEIP(String id) {
        return Builder.create(scope, id).build();
    }
}
