package org.bouncycastle.operator;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public interface KemEncapsulationLengthProvider
{
    int getEncapsulationLength(AlgorithmIdentifier kemAlgorithm);
}
