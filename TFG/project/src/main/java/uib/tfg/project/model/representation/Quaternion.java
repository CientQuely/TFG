package uib.tfg.project.model.representation;

/**
 * The Quaternion class. A Quaternion is a four-dimensional vector that is used to represent rotations of a rigid body
 * in the 3D space. It is very similar to a rotation vector; it contains an angle, encoded into the w component
 * and three components to describe the rotation-axis (encoded into x, y, z).
 * 
 * <p>
 * Quaternions allow for elegant descriptions of 3D rotations, interpolations as well as extrapolations and compared to
 * Euler angles, they don't suffer from gimbal lock. Interpolations between two Quaternions are called SLERP (Spherical
 * Linear Interpolation).
 * </p>
 * 
 * <p>
 * This class also contains the representation of the same rotation as a Quaternion and 4x4-Rotation-Matrix.
 * </p>
 * 
 * @author Leigh Beattie, Alexander Pacha
 * 
 */
public class Quaternion extends Vector4f {

    /**
     * Rotation matrix that contains the same rotation as the Quaternion in a 4x4 homogenised rotation matrix.
     * Remember that for performance reasons, this matrix is only updated, when it is accessed and not on every change
     * of the quaternion-values.
     */
    private MatrixF4x4 matrix;

    /**
     * This variable is used to synchronise the rotation matrix with the current quaternion values. If someone has
     * changed the
     * quaternion numbers then the matrix will need to be updated. To save on processing we only really want to update
     * the matrix when someone wants to fetch it, instead of whenever someone sets a quaternion value.
     */
    private boolean dirty = false;

    private Vector4f tmpVector = new Vector4f();
    private Quaternion tmpQuaternion;

    /**
     * Creates a new Quaternion object and initialises it with the identity Quaternion
     */
    public Quaternion() {
        super();
        matrix = new MatrixF4x4();
        loadIdentityQuat();
    }

    /**
     * Normalise this Quaternion into a unity Quaternion.
     */
    public void normalise() {
        this.dirty = true;
        float mag = (float) Math.sqrt(points[3] * points[3] + points[0] * points[0] + points[1] * points[1] + points[2]
                * points[2]);
        points[3] = points[3] / mag;
        points[0] = points[0] / mag;
        points[1] = points[1] / mag;
        points[2] = points[2] / mag;
    }

    @Override
    public void normalize() {
        normalise();
    }

    /**
     * Copies the values from the given quaternion to this one
     *
     * @param quat The quaternion to copy from
     */
    public void set(Quaternion quat) {
        this.dirty = true;
        copyVec4(quat);
    }

    /**
     * Multiply this quaternion by the input quaternion and store the result in the out quaternion
     *
     * @param input
     * @param output
     */
    public void multiplyByQuat(Quaternion input, Quaternion output) {

        if (input != output) {
            output.points[3] = (points[3] * input.points[3] - points[0] * input.points[0] - points[1] * input.points[1] - points[2]
                    * input.points[2]); //w = w1w2 - x1x2 - y1y2 - z1z2
            output.points[0] = (points[3] * input.points[0] + points[0] * input.points[3] + points[1] * input.points[2] - points[2]
                    * input.points[1]); //x = w1x2 + x1w2 + y1z2 - z1y2
            output.points[1] = (points[3] * input.points[1] + points[1] * input.points[3] + points[2] * input.points[0] - points[0]
                    * input.points[2]); //y = w1y2 + y1w2 + z1x2 - x1z2
            output.points[2] = (points[3] * input.points[2] + points[2] * input.points[3] + points[0] * input.points[1] - points[1]
                    * input.points[0]); //z = w1z2 + z1w2 + x1y2 - y1x2
        } else {
            tmpVector.points[0] = input.points[0];
            tmpVector.points[1] = input.points[1];
            tmpVector.points[2] = input.points[2];
            tmpVector.points[3] = input.points[3];

            output.points[3] = (points[3] * tmpVector.points[3] - points[0] * tmpVector.points[0] - points[1]
                    * tmpVector.points[1] - points[2] * tmpVector.points[2]); //w = w1w2 - x1x2 - y1y2 - z1z2
            output.points[0] = (points[3] * tmpVector.points[0] + points[0] * tmpVector.points[3] + points[1]
                    * tmpVector.points[2] - points[2] * tmpVector.points[1]); //x = w1x2 + x1w2 + y1z2 - z1y2
            output.points[1] = (points[3] * tmpVector.points[1] + points[1] * tmpVector.points[3] + points[2]
                    * tmpVector.points[0] - points[0] * tmpVector.points[2]); //y = w1y2 + y1w2 + z1x2 - x1z2
            output.points[2] = (points[3] * tmpVector.points[2] + points[2] * tmpVector.points[3] + points[0]
                    * tmpVector.points[1] - points[1] * tmpVector.points[0]); //z = w1z2 + z1w2 + x1y2 - y1x2
        }
    }

    public void multiplyByQuat(Quaternion input) {
        this.dirty = true;
        if (tmpQuaternion == null) tmpQuaternion = new Quaternion();
        tmpQuaternion.copyVec4(this);
        multiplyByQuat(input, tmpQuaternion);
        this.copyVec4(tmpQuaternion);
    }

    /**
     * Multiplies this Quaternion with a scalar
     *
     * @param scalar the value that the vector should be multiplied with
     */
    public void multiplyByScalar(float scalar) {
        this.dirty = true;
        multiplyByScalar(scalar);
    }

    /**
     * Add a quaternion to this quaternion
     *
     * @param input The quaternion that you want to add to this one
     */
    public void addQuat(Quaternion input) {
        this.dirty = true;
        addQuat(input, this);
    }

    /**
     * Add this quaternion and another quaternion together and store the result in the output quaternion
     *
     * @param input  The quaternion you want added to this quaternion
     * @param output The quaternion you want to store the output in.
     */
    public void addQuat(Quaternion input, Quaternion output) {
        output.setX(getX() + input.getX());
        output.setY(getY() + input.getY());
        output.setZ(getZ() + input.getZ());
        output.setW(getW() + input.getW());
    }

    /**
     * Subtract a quaternion to this quaternion
     *
     * @param input The quaternion that you want to subtracted from this one
     */
    public void subQuat(Quaternion input) {
        this.dirty = true;
        subQuat(input, this);
    }

    /**
     * Subtract another quaternion from this quaternion and store the result in the output quaternion
     *
     * @param input  The quaternion you want subtracted from this quaternion
     * @param output The quaternion you want to store the output in.
     */
    public void subQuat(Quaternion input, Quaternion output) {
        output.setX(getX() - input.getX());
        output.setY(getY() - input.getY());
        output.setZ(getZ() - input.getZ());
        output.setW(getW() - input.getW());
    }

    /**
     * Converts this Quaternion into the Rotation-Matrix representation which can be accessed by
     * {@link Quaternion#getMatrix4x4 getMatrix4x4}
     */
    private void convertQuatToMatrix() {
        float z = points[0];
        float y = points[1];
        float x = points[2];
        float w = points[3];

        matrix.setX0(1 - 2 * (y * y) - 2 * (z * z)); //1 - 2y2 - 2z2
        matrix.setX1(2 * (x * y) + 2 * (w * z)); // 2xy - 2wz
        matrix.setX2(2 * (x * z) - 2 * (w * y)); //2xz + 2wy
        matrix.setX3(0);
        matrix.setY0(2 * (x * y) - 2 * (w * z)); //2xy + 2wz
        matrix.setY1(1 - 2 * (x * x) - 2 * (z * z)); //1 - 2x2 - 2z2
        matrix.setY2(2 * (y * z) + 2 * (w * x)); // 2yz + 2wx
        matrix.setY3(0);
        matrix.setZ0(2 * (x * z) + 2 * (w * y)); //2xz + 2wy
        matrix.setZ1(2 * (y * z) - 2 * (w * x)); //2yz - 2wx
        matrix.setZ2(1 - 2 * (x * x) - 2 * (y * y)); //1 - 2x2 - 2y2
        matrix.setZ3(0);
        matrix.setW0(0);
        matrix.setW1(0);
        matrix.setW2(0);
        matrix.setW3(1);
    }

    /**
     * Get an axis angle representation of this quaternion.
     *
     * @param output Vector4f axis angle.
     */
    public void toAxisAngle(Vector4f output) {
        if (getW() > 1) {
            normalise(); // if w>1 acos and sqrt will produce errors, this cant happen if quaternion is normalised
        }
        float angle = 2 * (float) Math.toDegrees(Math.acos(getW()));
        float x;
        float y;
        float z;

        float s = (float) Math.sqrt(1 - getW() * getW()); // assuming quaternion normalised then w is less than 1, so term always positive.
        if (s < 0.001) { // test to avoid divide by zero, s is always positive due to sqrt
            // if s close to zero then direction of axis not important
            x = points[0]; // if it is important that axis is normalised then replace with x=1; y=z=0;
            y = points[1];
            z = points[2];
        } else {
            x = points[0] / s; // normalise axis
            y = points[1] / s;
            z = points[2] / s;
        }

        output.points[0] = x;
        output.points[1] = y;
        output.points[2] = z;
        output.points[3] = angle;
    }


    /**
     * Returns respectively, the pitch, yaw and roll of this quaternion as euler angles in the double array respectively
     *
     * @return An array of size 3 containing the euler angles for this quaternion
     * assumes is a normalised quaternion
     */
    public double[] toEuler() {
        double[] ret = new double[3];
        normalise();
        double w = getW();
        double x = getX();
        double y = getY();
        double z = getZ();
        double test = x * y + z * w;
        if (test > 0.499) { // singularity at north pole
            ret[0] = 2 * Math.atan2(x, w);
            ret[1] = Math.PI / 2;
            ret[2] = 0;
            return ret;
        }
        if (test < -0.499) { // singularity at south pole
            ret[0] = -2 * Math.atan2(x, w);
            ret[1] = -Math.PI / 2;
            ret[2] = 0;
            return ret;
        }
        double sqx = x * x;
        double sqy = y * y;
        double sqz = z * z;
        ret[0] = Math.atan2(2 * y * w - 2 * x * z, 1 - 2 * sqy - 2 * sqz);
        ret[1] = Math.asin(2 * test);
        ret[2] = Math.atan2(2 * x * w - 2 * y * z, 1 - 2 * sqx - 2 * sqz);
        return ret;
    }

    /**
     * Sets the quaternion to an identity quaternion of 0,0,0,1.
     */
    public void loadIdentityQuat() {
        this.dirty = true;
        setX(0);
        setY(0);
        setZ(0);
        setW(1);
    }

    @Override
    public String toString() {
        return "{X: " + getX() + ", Y:" + getY() + ", Z:" + getZ() + ", W:" + getW() + "}";
    }

    /**
     * This is an internal method used to build a quaternion from a rotation matrix and then sets the current quaternion
     * from that matrix.
     */
    private void generateQuaternionFromMatrix() {

        float qx;
        float qy;
        float qz;
        float qw;

        float[] mat = matrix.getMatrix();
        int[] indices = null;

        if (this.matrix.size() == 16) {
            if (this.matrix.isColumnMajor()) {
                indices = MatrixF4x4.matIndCol16_3x3;
            } else {
                indices = MatrixF4x4.matIndRow16_3x3;
            }
        } else {
            if (this.matrix.isColumnMajor()) {
                indices = MatrixF4x4.matIndCol9_3x3;
            } else {
                indices = MatrixF4x4.matIndRow9_3x3;
            }
        }

        int m00 = indices[0];
        int m01 = indices[1];
        int m02 = indices[2];

        int m10 = indices[3];
        int m11 = indices[4];
        int m12 = indices[5];

        int m20 = indices[6];
        int m21 = indices[7];
        int m22 = indices[8];

        float tr = mat[m00] + mat[m11] + mat[m22];
        if (tr > 0) {
            float s = (float) Math.sqrt(tr + 1.0) * 2; // S=4*qw 
            qw = 0.25f * s;
            qx = (mat[m21] - mat[m12]) / s;
            qy = (mat[m02] - mat[m20]) / s;
            qz = (mat[m10] - mat[m01]) / s;
        } else if ((mat[m00] > mat[m11]) & (mat[m00] > mat[m22])) {
            float s = (float) Math.sqrt(1.0 + mat[m00] - mat[m11] - mat[m22]) * 2; // S=4*qx 
            qw = (mat[m21] - mat[m12]) / s;
            qx = 0.25f * s;
            qy = (mat[m01] + mat[m10]) / s;
            qz = (mat[m02] + mat[m20]) / s;
        } else if (mat[m11] > mat[m22]) {
            float s = (float) Math.sqrt(1.0 + mat[m11] - mat[m00] - mat[m22]) * 2; // S=4*qy
            qw = (mat[m02] - mat[m20]) / s;
            qx = (mat[m01] + mat[m10]) / s;
            qy = 0.25f * s;
            qz = (mat[m12] + mat[m21]) / s;
        } else {
            float s = (float) Math.sqrt(1.0 + mat[m22] - mat[m00] - mat[m11]) * 2; // S=4*qz
            qw = (mat[m10] - mat[m01]) / s;
            qx = (mat[m02] + mat[m20]) / s;
            qy = (mat[m12] + mat[m21]) / s;
            qz = 0.25f * s;
        }

        setX(qx);
        setY(qy);
        setZ(qz);
        setW(qw);
    }

    /**
     * You can set the values for this quaternion based off a rotation matrix. If the matrix you supply is not a
     * rotation matrix this will fail. You MUST provide a 4x4 matrix.
     *
     * @param matrix A column major rotation matrix
     */
    public void setColumnMajor(float[] matrix) {

        this.matrix.setMatrix(matrix);
        this.matrix.setColumnMajor(true);

        generateQuaternionFromMatrix();
    }

    /**
     * You can set the values for this quaternion based off a rotation matrix. If the matrix you supply is not a
     * rotation matrix this will fail.
     *
     * @param matrix A column major rotation matrix
     */
    public void setRowMajor(float[] matrix) {

        this.matrix.setMatrix(matrix);
        this.matrix.setColumnMajor(false);

        generateQuaternionFromMatrix();
    }

    /**
     * Set this quaternion from axis angle values. All rotations are in degrees.
     *
     * @param azimuth The rotation around the z axis
     * @param pitch   The rotation around the y axis
     * @param roll    The rotation around the x axis
     */
    public void setEulerAngle(float azimuth, float pitch, float roll) {

        double heading = Math.toRadians(roll);
        double attitude = Math.toRadians(pitch);
        double bank = Math.toRadians(azimuth);

        double c1 = Math.cos(heading / 2);
        double s1 = Math.sin(heading / 2);
        double c2 = Math.cos(attitude / 2);
        double s2 = Math.sin(attitude / 2);
        double c3 = Math.cos(bank / 2);
        double s3 = Math.sin(bank / 2);
        double c1c2 = c1 * c2;
        double s1s2 = s1 * s2;
        setW((float) (c1c2 * c3 - s1s2 * s3));
        setX((float) (c1c2 * s3 + s1s2 * c3));
        setY((float) (s1 * c2 * c3 + c1 * s2 * s3));
        setZ((float) (c1 * s2 * c3 - s1 * c2 * s3));

        dirty = true;
    }

    /**
     * Rotation is in degrees. Set this quaternion from the supplied axis angle.
     *
     * @param vec The vector of rotation
     * @param rot The angle of rotation around that vector in degrees.
     */
    public void setAxisAngle(Vector3f vec, float rot) {
        double s = Math.sin(Math.toRadians(rot / 2));
        setX(vec.getX() * (float) s);
        setY(vec.getY() * (float) s);
        setZ(vec.getZ() * (float) s);
        setW((float) Math.cos(Math.toRadians(rot / 2)));

        dirty = true;
    }

    public void setAxisAngleRad(Vector3f vec, double rot) {
        double s = rot / 2;
        setX(vec.getX() * (float) s);
        setY(vec.getY() * (float) s);
        setZ(vec.getZ() * (float) s);
        setW((float) rot / 2);

        dirty = true;
    }

    /**
     * @return Returns this Quaternion in the Rotation Matrix representation
     */
    public MatrixF4x4 getMatrix4x4() {
        //toMatrixColMajor();
        if (dirty) {
            convertQuatToMatrix();
            dirty = false;
        }
        return this.matrix;
    }

    public void copyFromVec3(Vector3f vec, float w) {
        copyFromV3f(vec, w);
    }

    /**
     * Get a linear interpolation between this quaternion and the input quaternion, storing the result in the output
     * quaternion.
     *
     * @param input  The quaternion to be slerped with this quaternion.
     * @param output The quaternion to store the result in.
     * @param t      The ratio between the two quaternions where 0 <= t <= 1.0 . Increase value of t will bring rotation
     *               closer to the input quaternion.
     */
    public void slerp(Quaternion input, Quaternion output, float t) {
        // Calculate angle between them.
        //double cosHalftheta = this.dotProduct(input);
        Quaternion bufferQuat;
        float cosHalftheta = this.dotProduct(input);

        if (cosHalftheta < 0) {
            if (tmpQuaternion == null) tmpQuaternion = new Quaternion();
            bufferQuat = tmpQuaternion;
            cosHalftheta = -cosHalftheta;
            bufferQuat.points[0] = (-input.points[0]);
            bufferQuat.points[1] = (-input.points[1]);
            bufferQuat.points[2] = (-input.points[2]);
            bufferQuat.points[3] = (-input.points[3]);
        } else {
            bufferQuat = input;
        }
        /**
         * if(dot < 0.95f){
         * double angle = Math.acos(dot);
         * double ratioA = Math.sin((1 - t) * angle);
         * double ratioB = Math.sin(t * angle);
         * double divisor = Math.sin(angle);
         *
         * //Calculate Quaternion
         * output.setW((float)((this.getW() * ratioA + input.getW() * ratioB)/divisor));
         * output.setX((float)((this.getX() * ratioA + input.getX() * ratioB)/divisor));
         * output.setY((float)((this.getY() * ratioA + input.getY() * ratioB)/divisor));
         * output.setZ((float)((this.getZ() * ratioA + input.getZ() * ratioB)/divisor));
         * }
         * else{
         * lerp(input, output, t);
         * }
         */
        // if qa=qb or qa=-qb then theta = 0 and we can return qa
        if (Math.abs(cosHalftheta) >= 1.0) {
            output.points[0] = (this.points[0]);
            output.points[1] = (this.points[1]);
            output.points[2] = (this.points[2]);
            output.points[3] = (this.points[3]);
        } else {
            double sinHalfTheta = Math.sqrt(1.0 - cosHalftheta * cosHalftheta);
            // if theta = 180 degrees then result is not fully defined
            // we could rotate around any axis normal to qa or qb
            //if(Math.abs(sinHalfTheta) < 0.001){
            //output.setW(this.getW() * 0.5f + input.getW() * 0.5f);
            //output.setX(this.getX() * 0.5f + input.getX() * 0.5f);
            //output.setY(this.getY() * 0.5f + input.getY() * 0.5f);
            //output.setZ(this.getZ() * 0.5f + input.getZ() * 0.5f);
            //  lerp(bufferQuat, output, t);
            //}
            //else{
            double halfTheta = Math.acos(cosHalftheta);

            double ratioA = Math.sin((1 - t) * halfTheta) / sinHalfTheta;
            double ratioB = Math.sin(t * halfTheta) / sinHalfTheta;

            //Calculate Quaternion
            output.points[3] = ((float) (points[3] * ratioA + bufferQuat.points[3] * ratioB));
            output.points[0] = ((float) (this.points[0] * ratioA + bufferQuat.points[0] * ratioB));
            output.points[1] = ((float) (this.points[1] * ratioA + bufferQuat.points[1] * ratioB));
            output.points[2] = ((float) (this.points[2] * ratioA + bufferQuat.points[2] * ratioB));

            //}
        }
    }

    public double[] calculateRollPitchYawAngles(double[] angle) {
        float x = getX();
        float y = getY();
        float z = getZ();
        float w = getW();
        angle[0] = Math.atan2(2 * y * w + 2 * x * z, 1 - 2 * y * y - 2 * z * z);
        angle[1] = Math.atan2(2 * x * w + 2 * y * z, 1 - 2 * x * x - 2 * z * z);
        angle[2] = Math.asin(2 * x * y + 2 * z * w);
        return angle;
    }

    public Quaternion multiply(Quaternion a, Quaternion b) {
        Quaternion result = new Quaternion();
        float x1 = a.points[0];
        float y1 = a.points[1];
        float z1 = a.points[2];
        float w1 = a.points[3];
        float x2 = b.points[0];
        float y2 = b.points[1];
        float z2 = b.points[2];
        float w2 = b.points[3];

        result.points[3] = w2 * w1 - x2 * x1 - y2 * y1 - z2 * z1;

        result.points[0] = w2 * x1 + x2 * w1 - y2 * z1 + z2 * y1;

        result.points[1] = w2 * y1 + x2 * z1 + y2 * w1 - z2 * x1;

        result.points[2] = w2 * z1 - x2 * y1 + y2 * x1 + z2 * w1;

        return result;
    }


    private double[] quaternion_mult(double[] q, double[] r) {

        double[] result = {
                r[0] * q[0] - r[1] * q[1] - r[2] * q[2] - r[3] * q[3], // W
                r[0] * q[1] + r[1] * q[0] - r[2] * q[3] + r[3] * q[2], // X
                r[0] * q[2] + r[1] * q[3] + r[2] * q[0] - r[3] * q[1], // Y
                r[0] * q[3] - r[1] * q[2] + r[2] * q[1] + r[3] * q[0]};  // Z

        return result;
    }

    private double[] point_rotation_by_quaternion(double[] q_point, double[] q) {
        double[] q_conj = {q[0], -1 * q[1], -1 * q[2], -1 * q[3]};
        return quaternion_mult(quaternion_mult(q, q_point), q_conj);
    }

    public double[] rotateVector(double[] vector) {
        double[] q_point = {0, vector[0], vector[1], vector[2]};
        double[] q_rotation = {getW(), getX(), getY(), getZ()};

        double[] q_result = point_rotation_by_quaternion(q_point, q_rotation);
        double[] v_result = {q_result[1], q_result[2], q_result[3]};

        return v_result;
    }

    public double getNormalizedRollInDegrees() {
        double [] vector = {0, 0, 1};
        double [] rotationV = rotateVector(vector);

        double x = rotationV[0];
        double y = rotationV[1];
        double rotationY = Math.abs(y * 90);

        double rotation = 0;

        if( x >= 0 && y >= 0){
            rotation = rotationY;
        }else if( x <= 0 && y >= 0){
            rotation = 90 + (90 - rotationY);
        }else if( x <= 0 && y <= 0){
            rotation = 180 + rotationY;
        }else{
            // x > 0 && y < 0
            rotation = 270 + (90 - rotationY);
        }
        return rotation;
    }

    public double getNormalizedRoll() {
        double [] eulerAngles = toEuler();

        if ( eulerAngles[0] < 0){
            if ( eulerAngles[1] != Math.PI/2 && eulerAngles[1] != -Math.PI/2){
                eulerAngles[2] = - (eulerAngles[2] + Math.PI);
            }
        }

        return eulerAngles[2];
    }
}
