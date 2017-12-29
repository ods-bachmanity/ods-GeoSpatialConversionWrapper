package mil.nga.ods.geotrans.parameters;

import geotrans3.parameters.CoordinateSystemParameters;

public abstract class CoordinateSystemParamWrapper {

    protected CoordinateSystemParameters coordinateParams;

    public CoordinateSystemParameters getParameters() {
        return coordinateParams;
    }

    protected void setParameters(CoordinateSystemParameters cParams) {
        coordinateParams = cParams;
    }
}
