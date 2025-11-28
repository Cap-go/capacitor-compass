package app.capgo.capacitor.compass;

public class Vector {

    public float x;
    public float y;
    public float z;

    public Vector(float[] field) {
        this.x = field[0];
        this.y = field[1];
        this.z = field[2];
    }

    public Vector(Vector vector) {
        this.x = vector.x;
        this.y = vector.y;
        this.z = vector.z;
    }

    public Vector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void add(Vector vector) {
        this.x += vector.x;
        this.y += vector.y;
        this.z += vector.z;
    }

    public void multiply(double s) {
        this.x *= s;
        this.y *= s;
        this.z *= s;
    }

    public Vector crossProduct(Vector vector) {
        return new Vector(
            this.y * vector.z - this.z * vector.y,
            this.z * vector.x - this.x * vector.z,
            this.x * vector.y - this.y * vector.x
        );
    }

    public float dotProduct(Vector vector) {
        return this.x * vector.x + this.y * vector.y + this.z * vector.z;
    }

    public void normalize() {
        if (this.getLength() != 0) {
            this.multiply(1.0 / this.getLength());
        }
    }

    public float getLength() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public float getYaw() {
        return (float) Math.toDegrees(Math.atan2(this.y, this.x));
    }

    public float getPitch() {
        return (float) Math.toDegrees(Math.atan2(Math.sqrt(this.x * this.x + this.y * this.y), this.z));
    }
}
