package ru.kvinsoft.creanis.xmltest;

class Counter {
    private int c;

    public void counter(){
        this.c = 0;
    }

    void increase(){
        this.c++;
    }

    public void add(int a) { this.c += a; }

    public int get(){
        return this.c;
    }


}
