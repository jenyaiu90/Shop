package com.example.shop;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    public static final int price[] = { 30, 35, 50, 25, 20, 30, 10, 20 };
    public CharSequence getProductName(int id)
    {
        switch (id)
        {
        case 0:
            return getResources().getText(R.string.milk);
        case 1:
            return getResources().getText(R.string.bread);
        case 2:
            return getResources().getText(R.string.cheese);
        case 3:
            return getResources().getText(R.string.egg);
        case 4:
            return getResources().getText(R.string.tomato);
        case 5:
            return getResources().getText(R.string.apple);
        case 6:
            return getResources().getText(R.string.water);
        default:
            return getResources().getText(R.string.juice);
        }
    }
    Handler queueHandler, endHandler, startHandler;

    Button warehouseButton;
    TextView clientText[];
    Warehouse warehouse;
    Office office;
    Client client[];
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        warehouse = new Warehouse();
        office = new Office();
        warehouseButton = (Button)findViewById(R.id.warehouseButton);
        clientText = new TextView[6];
        clientText[0] = (TextView)findViewById(R.id.text0);
        clientText[1] = (TextView)findViewById(R.id.text1);
        clientText[2] = (TextView)findViewById(R.id.text2);
        clientText[3] = (TextView)findViewById(R.id.text3);
        clientText[4] = (TextView)findViewById(R.id.text4);
        clientText[5] = (TextView)findViewById(R.id.text5);
        queueHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                clientText[msg.what].setBackgroundColor(getResources().getColor(R.color.queue));
                CharSequence tmp = clientText[msg.what].getText();
                clientText[msg.what].setText(getResources().getText(R.string.queue) + "\n" + tmp);
            }
        };
        endHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                clientText[msg.what].setBackgroundColor(getResources().getColor(R.color.end));
                clientText[msg.what].setText(R.string.end);
                while (client[msg.what].getState() == Thread.State.RUNNABLE);
                client[msg.what] = new Client(msg.what);
                client[msg.what].start();
            }
        };
        startHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                clientText[msg.what].setBackgroundColor(getResources().getColor(R.color.searching));
                clientText[msg.what].setText(R.string.searching);
            }
        };
        client = new Client[6];
        for (int i = 0; i < 6; i++)
        {
            client[i] = new Client(i);
            client[i].start();
        }
    }
    public void warehouseClick(View view)
    {

    }

    private class Warehouse
    {
        public static final int N = 8;
        private int products[];

        public Warehouse()
        {
            products = new int[N];
            for (int i = 0; i < N; i++)
            {
                products[i] = 20;
            }
        }

        public int count(int id)
        {
            if (id < N && id >= 0)
            {
                return products[id];
            } else
            {
                return 0;
            }
        }

        public void take(int clientId, int id, int num)
        {
            try
            {
                products[id] -= num;
                if (products[id] < 0)
                {
                    Exception e = new Exception("Warehouse doesn`t have enough products");
                    throw e;
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            CharSequence tmp = clientText[clientId].getText();
            clientText[clientId].setText(tmp + "\n" + getProductName(id) + ": " + num);

        }
    }
    private class Office
    {
        public void pay(int clientId, int id[], int count[])
        {
            int cost = 0;
            for (int i = 0; i < id.length; i++)
            {
                cost += price[id[i]] * count[i];
            }
            clientText[clientId].setBackgroundColor(getResources().getColor((R.color.paying)));
            clientText[clientId].setText(getResources().getText(R.string.paying).toString() + cost);
        }
    }
    private class Client extends Thread
    {
        int id;
        int wantId[], wantCount[];
        public Client(int i)
        {
            super();
            id = i;
        }
        private void init()
        {
            wantId = new int[(int)(Math.random() * 5 + 1)];
            wantCount = new int[wantId.length];
            for (int i = 0; i < wantId.length; i++)
            {
                wantId[i] = (int)(Math.random() * 7);
                wantCount[i] = (int)(Math.random() * 6 + 1);
            }
        }
        @Override
        public void run()
        {
            init();
            try
            {
                Thread.sleep((int)(Math.random() * 5000 + 1000));
                startHandler.sendEmptyMessage(id);
                for (int i = 0; i < wantId.length; i++)
                {
                    Thread.sleep((int)(Math.random() * 5000 + 1000));
                    synchronized (warehouse)
                    {
                        if (warehouse.count(wantId[i]) < wantCount[i])
                        {
                            wantCount[i] = warehouse.count(wantId[i]);
                        }
                        warehouse.take(id, wantId[i], wantCount[i]);
                    }
                }
                Thread.sleep((int)(Math.random() * 5000 + 1000));
                queueHandler.sendEmptyMessage(id);
                synchronized (office)
                {
                    office.pay(id, wantId, wantCount);
                    Thread.sleep((int)(Math.random() * 3000 + 1000));
                }
                endHandler.sendEmptyMessage(id);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
