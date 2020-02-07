package comp557.a4;

public class FresnelReflectance {
	
	public static double FrDielectric(double cosThetaI, double etaI, double etaT) {
		cosThetaI = Util.clamp(cosThetaI, -1, 1);
		boolean entering = cosThetaI > 0;
		if (!entering) {
			double temp = etaI;
			etaI = etaT;
			etaT = temp;
			cosThetaI = Math.abs(cosThetaI);
		}
		
		//calculate the refracted angle
		double sinThetaI = Math.sqrt(Math.max(0, 1 - cosThetaI * cosThetaI));
		double sinThetaT = etaI / etaT * sinThetaI;
		if (sinThetaT >= 1)
			return 1;
		double cosThetaT = Math.sqrt(Math.max(0, 1 - sinThetaT * sinThetaT));
	
		//fresnel value
		double reflectance_l = ((etaT * cosThetaI) - (etaI * cosThetaT)) /
                				((etaT * cosThetaI) + (etaI * cosThetaT));
		double reflectance_p = ((etaI * cosThetaI) - (etaT * cosThetaT)) /
                				((etaI * cosThetaI) + (etaT * cosThetaT));
		return (reflectance_l * reflectance_l + reflectance_p * reflectance_p) / 2;
	}
}
