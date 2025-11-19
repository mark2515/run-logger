package moe.kunlonghe.myruns;

class WekaClassifier {

  public static double classify(Object[] i)
    throws Exception {

    double p = Double.NaN;
    p = WekaClassifier.N74ea37ad4(i);
    return p;
  }
  static double N74ea37ad4(Object []i) {
    double p = Double.NaN;
    if (i[0] == null) {
      p = 0;
    } else if (((Double) i[0]).doubleValue() <= 13.390311) {
      p = 0;
    } else if (((Double) i[0]).doubleValue() > 13.390311) {
    p = WekaClassifier.N2f5821155(i);
    } 
    return p;
  }
  static double N2f5821155(Object []i) {
    double p = Double.NaN;
    if (i[64] == null) {
      p = 1;
    } else if (((Double) i[64]).doubleValue() <= 14.534508) {
    p = WekaClassifier.N1affe7d96(i);
    } else if (((Double) i[64]).doubleValue() > 14.534508) {
      p = 2;
    } 
    return p;
  }
  static double N1affe7d96(Object []i) {
    double p = Double.NaN;
    if (i[4] == null) {
      p = 1;
    } else if (((Double) i[4]).doubleValue() <= 14.034383) {
    p = WekaClassifier.N16ed48c07(i);
    } else if (((Double) i[4]).doubleValue() > 14.034383) {
      p = 1;
    } 
    return p;
  }
  static double N16ed48c07(Object []i) {
    double p = Double.NaN;
    if (i[7] == null) {
      p = 1;
    } else if (((Double) i[7]).doubleValue() <= 4.804712) {
      p = 1;
    } else if (((Double) i[7]).doubleValue() > 4.804712) {
      p = 2;
    } 
    return p;
  }
}
