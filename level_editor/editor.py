import json

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

    def open_gui(self):
        if self.active_element.isInstance(Target):
            # open menu with fields
            pass
        elif self.active_element.isInstance(FactNode):
            # open menu with fields
            pass
        else:
            pass

    def close_gui(self):
        if self.active_element.isInstance(Target):
            # save contents
            pass
        elif self.active_element.isInstance(FactNode):
            # save contents
            pass
        else:
            pass

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



