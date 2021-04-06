import json
import tkinter as tk



class Target:
    def __init__(self, contents = None):
        if contents == None:
            self.combos = set()
            self.pos = (0,0)
            self.name = ''
            self.suspicion = 1
            self.max_stress = 100
            self.neighbors = set()
            self.starting_stress = 0
            # self.factnodes = dict()
        else:
            self.name = contents['name']
            self.combos = {(combo['overwritten'], combo['summary'], int(combo['modified_dam']), combo['nodes'])
                                    for combo in contents['combos']}
            self.pos = (int(contents['pos'][0]), int(contents['pos'][1]))
            self.suspicion = int(contents['suspicion'])
            self.max_stress = int(contents['max_stress'])
            self.starting_stress = int(contents['starting_stress'])
            self.neighbors = set(contents['neighbors'])
            # self.factnodes

    # def open_gui(self):
    #     pass

    # def close_gui(self):
    #     pass

class FactNode:
    def __init__(self, pos = (0,0), contents = None):
        '''
            contents is a dictionary containing factnode data 
        '''
        if contents == None:
            self.name = ''
            self.title = ''
            self.position = pos
            self.children = set()
            # self.parent = None
            self.connection_to_parent = list()
            self.player_stress_dam = 0
            self.stress_dam = 0
            self.summary = ''
            self.contents = ''
            self.locked = True
        else:
            self.name = contents['name']
            self.title = contents['title']
            self.position = (int(contents['pos'][0]), int(contents['pos'][1]))
            self.children = {FactNode(contents = factnode) for factnode in contents['children']}
            # self.parent =
            self.connection_to_parent = [(int(con_route[0]), int(con_route[1])) for con_route in contents['connection_to_parent']]
            self.player_stress_dam = int(contents['player_stress_dam'])
            self.stress_dam = contents['stress_dam']
            self.summary = contents['summary']
            self.contents = contents['contents']
            self.locked = bool(contents['locked'])

    # def open_gui(self):
    #     pass

    # def close_gui(self):
    #     pass


class View:
    def __init__(self):
        self.root = tk.Tk('main')
        self.canvas = tk.Canvas(self.root, height=1000, width=1000, bg='white')
        self.root.title('very serious legit level editor')
        self.canvas.pack(fill=tk.BOTH, expand=True)
        self.canvas.bind('<Configure>', self.draw_grid)
        self.canvas.bind('<Button-1>', self.draw_object)
        self.root.mainloop()

    def draw_grid(self, event = None, rows = 13, cols = 13):
        w = self.canvas.winfo_width()
        h = self.canvas.winfo_height()
        self.canvas.delete('grid_line')
        #self.canvas.delete('grid_line')

        for i in range(0, w, int(w/rows)):
            self.canvas.create_line([i, 0], [i, h], tag = 'grid_line')
        
        for i in range(0, h, int(h/cols)):
            self.canvas.create_line([0, i], [w, i], tag = 'grid_line')

        #self.canvas.create_text(w/2, h/2 , font = "WingDings", text = 'T', tag = 'target')

    def draw_object(self, event, row= 1, col= 1):
        w = self.canvas.winfo_width()
        h = self.canvas.winfo_height()
        self.canvas.delete('target')
        self.canvas.create_text((w/13)* , (h/13) , font = "WingDings", text = 'T', tag = 'target')

    def open_gui(self, active_element):
        if isinstance(active_element, Target):
            # open menu with fields
            pass
        elif isinstance(active_element, FactNode):
            # open menu with fields
            pass
        else:
            pass

    def close_gui(self, active_element):
        if isinstance(active_element, Target):
            # save contents
            pass
        elif isinstance(active_element, FactNode):
            # save contents
            pass
        else:
            pass

class Controller:
    def __init__(self):
        self.a = 'b'
        
class Level:
    def __init__(self, file = None):
        if file == None:
            self.target = Target()
            self.factnodes = set()
        else:
            jason = json.load(file)
            self.target = Target(contents = jason)
            self.factnodes = { FactNode(contents = cont) for cont in jason['factnodes'] }
        self.active_element = None 
        self.drawing = False # self.drawing if drawing a FactNode path
        self.view = View()


    def element_at(self, pos):
        pos = (int(pos[0]), int(pos[1]))
        if pos == 0:
            return self.target
        else:
            for factnode in self.factnodes:
                if factnode.pos == pos:
                    return factnode
        return None

    def click(self, pos):
        if self.drawing:
            pass
        if self.active_element != None:
            self.active_element.close_gui()
        self.active_element = self.click(pos)
        if self.active_element != None:
            self.active_element.open_gui()


l = Level()
l.view.draw_object()
