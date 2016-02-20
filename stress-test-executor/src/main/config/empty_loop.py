import time
import numpy as np
# 可以自己import我们平台支持的第三方python模块，比如pandas、numpy等。

# 在这个方法中编写任何的初始化逻辑。context对象将会在你的算法策略的任何方法之间做传递。
def init(context):
    context.stocks = index_components('000300.XSHG')[:20]

    update_universe(context.stocks)
    context.history_total = 0
    context.loop_total = 0
    context.orders_sent = 0
    context.order_time = 0
    context.np_time = 0
    # 实时打印日志
    # logger.info("Interested at stock: " + str(context.stocks))

# before_trading此函数会在每天交易开始前被调用，当天只会被调用一次
def before_trading(context, bar_dict):
    per_order = 0
    if context.orders_sent > 0:
        per_order = context.order_time/context.orders_sent
    print('history cost: ' + str(context.history_total) + ', loop cost: ' + str(context.loop_total) + ', order cost: ' + str(context.order_time) + ', np cost: ' + str(context.np_time))
    context.history_total = 0
    context.loop_total = 0
    context.orders_sent = 0
    context.order_time = 0
    context.np_time = 0
    pass


# 你选择的证券的数据更新将会触发此段逻辑，例如日或分钟历史数据切片或者是实时数据切片更新
def handle_bar(context, bar_dict):

    history_start = time.time()
    h = history(120, '1m', 'close')

    history_end = time.time()
    context.history_total += history_end - history_start

    for s in context.stocks:
        if s not in h.keys():
            continue

        try:
            np_start = time.time()
            ma1 = np.mean(h[s])
            ma2 = np.mean(h[s][-10:])
            context.np_time += time.time() - np_start
        except TypeError:
            print (h[s])
        signal = ma2 - ma1
        if signal > 0 and context.portfolio.positions[s].quantity == 0:
            context.orders_sent+=1
            order_start = time.time()
            order_lots(s, 1)
            context.order_time += time.time() - order_start

        if signal < 0 and context.portfolio.positions[s].sellable > 0:
            context.orders_sent+=1
            order_start = time.time()
            order_shares(s, -context.portfolio.positions[s].sellable)
            context.order_time += time.time() - order_start

    context.loop_total += time.time() - history_end

    pass
